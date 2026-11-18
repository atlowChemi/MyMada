import { readFileSync, appendFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { pathToFileURL } from 'node:url';
import { request } from './play-preflight.mjs';

export function validateRelease(operation, code, name) {
  if (!['validate-beta', 'publish-beta'].includes(operation)) throw new Error('Beta operation required');
  if (!/^[1-9]\d*$/.test(code) || Number(code) > 2100000000) throw new Error('Invalid version code');
  if (!/^\d+\.\d+\.\d+-beta\.\d+$/.test(name)) throw new Error('Expected version name such as 4.0.0-beta.5');
}

export async function releaseBeta(token, operation, code, name, bundlePath) {
  validateRelease(operation, code, name);
  if (!token) throw new Error('PLAY_ACCESS_TOKEN is required');
  const bundle = readFileSync(bundlePath);
  if (!bundle.length) throw new Error('Empty bundle');
  const sha256 = createHash('sha256').update(bundle).digest('hex');
  const edit = await request(token, '', 'POST', {});
  if (!/^\d+$/.test(edit.id)) throw new Error('Play returned an invalid edit ID');
  let committed = false;
  let failure;
  try {
    const { tracks = [] } = await request(token, `/${edit.id}/tracks`);
    const { bundles = [] } = await request(token, `/${edit.id}/bundles`);
    const { apks = [] } = await request(token, `/${edit.id}/apks`);
    const highest = Math.max(0, ...bundles.map(b => Number(b.versionCode)), ...apks.map(a => Number(a.versionCode)),
      ...tracks.flatMap(t => (t.releases ?? []).flatMap(r => (r.versionCodes ?? []).map(Number))));
    if (!Number.isFinite(highest) || Number(code) <= highest) throw new Error(`Version code must exceed Play inventory maximum ${highest}`);
    const beta = tracks.find(t => t.track === 'beta');
    if (!beta || (beta.releases ?? []).some(r => r.status !== 'completed'))
      throw new Error('Expected existing beta track with no pending/staged release; inspect Play first');
    const response = await fetch(`https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/atlow.chemi.mymada/edits/${edit.id}/bundles?uploadType=media`, {
      method: 'POST', headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/octet-stream' },
      body: bundle, signal: AbortSignal.timeout(300_000),
    });
    if (!response.ok) throw new Error(`Play bundle upload: ${response.status} ${await response.text()}`);
    const uploaded = await response.json();
    if (String(uploaded.versionCode) !== code || uploaded.sha256?.toLowerCase() !== sha256)
      throw new Error('Uploaded bundle version/digest does not match the signed candidate');
    await request(token, `/${edit.id}/tracks/beta`, 'PUT', { track: 'beta', releases: [{
      name, versionCodes: [code], status: 'completed', inAppUpdatePriority: 2,
      releaseNotes: [
        { language: 'en-US', text: 'Native beta update addressing the React Native launch-argument crash reported on Xiaomi devices. Please report any regressions during testing.' },
        { language: 'iw-IL', text: 'עדכון בטא בגרסה טבעית לטיפול בקריסת React Native בעת פתיחה שדווחה במכשירי Xiaomi. נשמח לדיווח על תקלות במהלך הבדיקות.' },
      ],
    }] });
    await request(token, `/${edit.id}:validate`, 'POST', {});
    if (operation === 'publish-beta') {
      // Submit automatically, but never cancel an existing review.
      // Commit is never retried: a lost response requires inspecting Play before another attempt.
      await request(token, `/${edit.id}:commit?changesInReviewBehavior=ERROR_IF_IN_REVIEW`, 'POST', {});
      committed = true;
    }
    return { operation, track: 'beta', versionCode: code, versionName: name, sha256, committed,
      reviewSubmission: committed ? 'submitted-automatically' : 'not-committed' };
  } catch (error) {
    failure = error;
    throw error;
  } finally {
    if (!committed) {
      try { await request(token, `/${edit.id}`, 'DELETE'); }
      catch (cleanupError) {
        if (failure) throw new AggregateError([failure, cleanupError], 'Play release and edit cleanup both failed; inspect Play before retrying');
        throw cleanupError;
      }
    }
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const report = JSON.stringify(await releaseBeta(process.env.PLAY_ACCESS_TOKEN,
      process.env.RELEASE_OPERATION, process.env.RELEASE_VERSION_CODE,
      process.env.RELEASE_VERSION_NAME, process.env.RELEASE_BUNDLE), null, 2);
    console.log(report);
    if (process.env.GITHUB_STEP_SUMMARY) appendFileSync(process.env.GITHUB_STEP_SUMMARY, `## Beta release\n\n\`\`\`json\n${report}\n\`\`\`\n`);
  } catch (error) {
    console.error(error.message);
    if (error instanceof AggregateError) error.errors.forEach(e => console.error(e.message));
    process.exitCode = 1;
  }
}
