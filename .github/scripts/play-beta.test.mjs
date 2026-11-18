import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';
import { releaseBeta } from './play-beta.mjs';

test('rejects invalid release inputs and missing auth before network or file access', async (t) => {
  const mocked = t.mock.method(globalThis, 'fetch', () => assert.fail('unexpected request'));
  for (const args of [
    ['fixture', 'production', '12828', '4.0.0-beta.5'],
    ['fixture', 'publish-beta', '0', '4.0.0-beta.5'],
    ['fixture', 'publish-beta', '2100000001', '4.0.0-beta.5'],
    ['fixture', 'publish-beta', '12828; command', '4.0.0-beta.5'],
    ['fixture', 'publish-beta', '12828', '4.0.0'],
    ['', 'validate-beta', '12828', '4.0.0-beta.5'],
  ]) await assert.rejects(releaseBeta(...args, '/nonexistent'));
  assert.equal(mocked.mock.callCount(), 0);
});

for (const scenario of ['validate', 'publish', 'used-code', 'pending-beta', 'upload-denied', 'wrong-digest', 'wrong-code', 'validation-denied', 'commit-unknown']) {
  test(`beta transaction: ${scenario}`, async (t) => {
    const directory = mkdtempSync(`${tmpdir()}/play-beta-test-`);
    t.after(() => rmSync(directory, { recursive: true, force: true }));
    const bundle = Buffer.from('signed candidate fixture');
    const bundlePath = `${directory}/candidate.aab`;
    writeFileSync(bundlePath, bundle);
    const digest = createHash('sha256').update(bundle).digest('hex');
    const originalProduction = { track: 'production', releases: [{ name: '3.5.7', versionCodes: ['12822'], status: 'completed' }] };
    let betaWrite;
    const mocked = t.mock.method(globalThis, 'fetch', async (url, options) => {
      assert.equal(options.headers.Authorization, 'Bearer fixture');
      const path = new URL(url).pathname;
      if (path.endsWith('/edits')) return Response.json({ id: '123' });
      if (options.method === 'DELETE') return new Response(null, { status: 204 });
      if (path.endsWith('/tracks')) return Response.json({ tracks: [originalProduction,
        { track: 'beta', releases: [{ versionCodes: ['12827'], status: scenario === 'pending-beta' ? 'draft' : 'completed' }] },
      ] });
      if (path.endsWith('/bundles') && options.method === 'GET')
        return Response.json({ bundles: [{ versionCode: scenario === 'used-code' ? 12828 : 12827 }] });
      if (path.endsWith('/apks')) return Response.json({ apks: [] });
      if (path.includes('/upload/')) {
        assert.deepEqual(options.body, bundle);
        if (scenario === 'upload-denied') return new Response('wrong upload key', { status: 403 });
        return Response.json({ versionCode: scenario === 'wrong-code' ? 12829 : 12828,
          sha256: scenario === 'wrong-digest' ? 'bad' : digest });
      }
      if (path.endsWith('/tracks/beta')) {
        assert.equal(options.method, 'PUT');
        betaWrite = JSON.parse(options.body);
        assert.equal(betaWrite.track, 'beta');
        assert.deepEqual(betaWrite.releases[0].versionCodes, ['12828']);
        assert.equal(betaWrite.releases[0].status, 'completed');
        return Response.json(betaWrite);
      }
      if (path.endsWith(':validate')) {
        assert.equal(new URL(url).searchParams.get('changesNotSentForReview'), 'true');
        return scenario === 'validation-denied'
          ? new Response('invalid release', { status: 400 }) : Response.json({ id: '123' });
      }
      if (path.endsWith(':commit')) {
        assert.equal(new URL(url).searchParams.get('changesNotSentForReview'), 'true');
        assert.ok(betaWrite);
        if (scenario === 'commit-unknown') throw new Error('connection lost after commit request');
        return Response.json({ id: '123' });
      }
      assert.fail(`Unexpected request ${options.method} ${path}`);
    });
    const operation = ['publish', 'commit-unknown'].includes(scenario) ? 'publish-beta' : 'validate-beta';
    const result = releaseBeta('fixture', operation, '12828', '4.0.0-beta.5', bundlePath);
    if (['validate', 'publish'].includes(scenario)) assert.deepEqual(await result, {
      operation, track: 'beta', versionCode: '12828', versionName: '4.0.0-beta.5', sha256: digest, committed: scenario === 'publish',
      reviewSubmission: scenario === 'publish' ? 'required-in-play-console' : 'not-committed',
    });
    else await assert.rejects(result);
    const calls = mocked.mock.calls.map(({ arguments: [url, options] }) => ({ url, method: options.method }));
    assert.ok(calls.every(c => !c.url.includes('/tracks/production')));
    assert.deepEqual(originalProduction.releases[0].versionCodes, ['12822']);
    assert.equal(calls.filter(c => new URL(c.url).pathname.endsWith(':commit')).length, ['publish', 'commit-unknown'].includes(scenario) ? 1 : 0);
    assert.equal(calls.filter(c => c.method === 'DELETE').length, scenario === 'publish' ? 0 : 1);
    if (['used-code', 'pending-beta'].includes(scenario)) assert.ok(calls.every(c => !c.url.includes('/upload/')));
  });
}
