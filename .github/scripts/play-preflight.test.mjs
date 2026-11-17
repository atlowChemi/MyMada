import { test } from 'node:test';
import assert from 'node:assert/strict';
import { inspectPlay } from './play-preflight.mjs';

test('inspects releases and deletes its own edit on success or API failure', async () => {
  for (const failTracks of [false, true]) {
    const calls = [];
    const fetchApi = async (url, options) => {
      calls.push([options.method, new URL(url).pathname.split('/edits')[1]]);
      assert.equal(options.headers.Authorization, 'Bearer fixture');
      if (options.method === 'POST') return Response.json({ id: '123' });
      if (options.method === 'DELETE') return new Response(null, { status: 204 });
      if (url.endsWith('/tracks')) return failTracks
        ? new Response('permission denied', { status: 403 })
        : Response.json({ tracks: [{ track: 'internal', releases: [{ versionCodes: ['12827'] }] }] });
      if (url.endsWith('/bundles')) return Response.json({ bundles: [{ versionCode: 12827 }] });
      return Response.json({ apks: [{ versionCode: 12826 }] });
    };
    if (failTracks) await assert.rejects(inspectPlay('fixture', fetchApi), /403/);
    else assert.deepEqual(await inspectPlay('fixture', fetchApi), {
      packageName: 'atlow.chemi.mymada',
      tracks: [{ track: 'internal', releases: [{ versionCodes: ['12827'] }] }],
      bundleVersionCodes: [12827], apkVersionCodes: [12826],
    });
    assert.deepEqual(calls.at(-1), ['DELETE', '/123']);
    assert.equal(calls.filter(([method]) => method === 'POST').length, 1);
    assert.ok(calls.every(([, path]) => !path.includes(':commit')));
  }
});

test('missing auth makes no request, and cleanup failure cannot look successful', async () => {
  await assert.rejects(inspectPlay('', () => assert.fail('unexpected request')), /required/);
  await assert.rejects(inspectPlay('fixture', async (_, options) => {
    if (options.method === 'POST') return Response.json({ id: '123' });
    if (options.method === 'DELETE') return new Response('unavailable', { status: 503 });
    return Response.json({});
  }), /503/);
});
