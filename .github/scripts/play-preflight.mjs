import { appendFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';

async function request(token, path, method = 'GET', body) {
  const base = 'https://androidpublisher.googleapis.com/androidpublisher/v3/applications/atlow.chemi.mymada/edits';
  const response = await fetch(base + path, {
    method,
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    signal: AbortSignal.timeout(60_000),
  });
  if (!response.ok) throw new Error(`Play ${method} ${path}: ${response.status} ${await response.text()}`);
  return response.status === 204 ? undefined : response.json();
}

// Play exposes tracks/bundles through an edit. Never commit this inspection edit.
export async function inspectPlay(token) {
  if (!token) throw new Error('PLAY_ACCESS_TOKEN is required');
  const edit = await request(token, '', 'POST', {});
  if (!/^\d+$/.test(edit.id)) throw new Error('Play returned an invalid edit ID');
  let failure;
  try {
    const tracks = await request(token, `/${edit.id}/tracks`);
    const bundles = await request(token, `/${edit.id}/bundles`);
    const apks = await request(token, `/${edit.id}/apks`);
    return {
      packageName: 'atlow.chemi.mymada',
      tracks: tracks.tracks ?? [],
      bundleVersionCodes: (bundles.bundles ?? []).map(b => b.versionCode),
      apkVersionCodes: (apks.apks ?? []).map(a => a.versionCode),
    };
  } catch (error) {
    failure = error;
    throw error;
  } finally {
    try { await request(token, `/${edit.id}`, 'DELETE'); }
    catch (cleanupError) {
      if (failure) throw new AggregateError([failure, cleanupError], 'Play inspection and edit cleanup both failed');
      throw cleanupError;
    }
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const report = JSON.stringify(await inspectPlay(process.env.PLAY_ACCESS_TOKEN), null, 2);
    console.log(report);
    if (process.env.GITHUB_STEP_SUMMARY) appendFileSync(process.env.GITHUB_STEP_SUMMARY,
      `## Play release inventory\n\nTemporary edit deleted without committing.\n\n\`\`\`json\n${report}\n\`\`\`\n`);
  } catch (error) {
    console.error(error.message);
    if (error instanceof AggregateError) error.errors.forEach(e => console.error(e.message));
    process.exitCode = 1;
  }
}
