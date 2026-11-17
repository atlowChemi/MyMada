import { test } from 'node:test';
import assert from 'node:assert/strict';
import { inspectPlay } from './play-preflight.mjs';

for (const failTracks of [false, true]) {
  test(`deletes its own edit after ${failTracks ? 'API failure' : 'successful release inspection'}`, async (t) => {
    const fetchMock = t.mock.method(globalThis, 'fetch', async (url, options) => {
      assert.equal(options.headers.Authorization, 'Bearer fixture');
      if (options.method === 'POST') return Response.json({ id: '123' });
      if (options.method === 'DELETE') return new Response(null, { status: 204 });
      if (url.endsWith('/tracks')) return failTracks
        ? new Response('permission denied', { status: 403 })
        : Response.json({ tracks: [{ track: 'internal', releases: [{ versionCodes: ['12827'] }] }] });
      if (url.endsWith('/bundles')) return Response.json({ bundles: [{ versionCode: 12827 }] });
      return Response.json({ apks: [{ versionCode: 12826 }] });
    });
    if (failTracks) await assert.rejects(inspectPlay('fixture'), /403/);
    else assert.deepEqual(await inspectPlay('fixture'), {
      packageName: 'atlow.chemi.mymada',
      tracks: [{ track: 'internal', releases: [{ versionCodes: ['12827'] }] }],
      bundleVersionCodes: [12827], apkVersionCodes: [12826],
    });
    const calls = fetchMock.mock.calls.map(({ arguments: [url, options] }) =>
      [options.method, new URL(url).pathname.split('/edits')[1]]);
    assert.deepEqual(calls.at(-1), ['DELETE', '/123']);
    assert.equal(calls.filter(([method]) => method === 'POST').length, 1);
    assert.ok(calls.every(([, path]) => !path.includes(':commit')));
  });
}

test('missing auth makes no request, and cleanup failure cannot look successful', async (t) => {
  const fetchMock = t.mock.method(globalThis, 'fetch', () => assert.fail('unexpected request'));
  await assert.rejects(inspectPlay(''), /required/);
  assert.equal(fetchMock.mock.callCount(), 0);
  fetchMock.mock.mockImplementation(async (_, options) => {
    if (options.method === 'POST') return Response.json({ id: '123' });
    if (options.method === 'DELETE') return new Response('unavailable', { status: 503 });
    return Response.json({});
  });
  await assert.rejects(inspectPlay('fixture'), /503/);
});
