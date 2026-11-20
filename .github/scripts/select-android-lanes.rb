#!/usr/bin/env ruby
require 'json'
require 'open3'
require 'yaml'

# Shared with the full-inventory checker; method-specific exclusions do not
# exclude the rest of a class.
def assigned?(job, test)
  klass = test.split('#').first
  excluded = job.fetch('exclude', '').split(',')
  return false if excluded.include?(klass) || excluded.include?(test)
  job.fetch('tests', '').split(',').any? { |selector| selector == klass || selector == test } ||
    job.fetch('packages', '').split(',').any? { |package| klass.start_with?(package + '.') }
end

BASELINE = %w[navigation-and-compatibility settings-and-history startup-upgrade widget-reinstall platform-alone].freeze
DOMAIN_LANES = {
  'oxygen' => /\A(tools-and-dictionary|oxygen-hebrew)\z/,
  # MedicalLocalAcceptanceTest also consumes DictionaryModel and VitalsScreen.
  'tools' => /\A(tools-and-dictionary|vitals-.*|home-and-integrations|integrations-hebrew)\z/,
  'documents' => /\Adocument-policy-fixtures\z/,
  'home' => /\A(home-and-integrations|integrations-hebrew|chat-theme-alone|document-policy-fixtures)\z/,
  # Call delivery and audio share settings, permissions, overlays and entry points.
  'calls' => /\A(call-|listener-|monitor-|overlay-|notification-|detection-|metronome-|audio-|sound-|external-|platform-after-|home-|integrations-|chat-)/
}.freeze

def domain(path)
  return 'docs' if path.match?(%r{\A(?:docs/.*|[^/]+\.md|LICENSE(?:\.[^/]+)?)\z})
  source = path[%r{\Aapp/src/(?:main|test)/java/atlow/chemi/mymada/(.+\.kt)\z}, 1]
  return nil unless source
  case source
  when %r{\A(?:oxygen/|fragments/OxyCalc\.kt\z)} then 'oxygen'
  when %r{\A(?:dictionary/|contractions/|tools/(?:apgar|glasgow|pulse|vitals)/|fragments/(?:ApgarCalc|GlazgoCalc|PulseCalc|VitalsChecker|DictionaryAdmin|ContracTimer)\.kt\z)} then 'tools'
  when %r{\A(?:documents/|privacy/|PrivacyActivity\.kt\z)} then 'documents'
  when %r{\Aabout/} then 'home'
  when %r{\A(?:home/|integrations/|calls/|permissions/|recieversAndServices/|NotificationService\.kt\z|classes/(?:AudioGenerator|Metronome)\.kt\z|fragments/MetroActivity\.kt\z)} then 'calls'
  end
end

def select_lanes(lanes, paths, full_reason: nil)
  return [lanes, full_reason] if full_reason
  uncovered = lanes.find do |lane|
    !BASELINE.include?(lane.fetch('case')) &&
      DOMAIN_LANES.values.none? { |pattern| lane.fetch('case').match?(pattern) }
  end
  return [lanes, "Unmapped lane #{uncovered.fetch('case')}: full coverage"] if uncovered
  return [lanes, 'Empty or unavailable diff: full coverage'] if paths.nil? || paths.empty?
  selected = []
  code_changed = false
  paths.each do |path|
    next if domain(path) == 'docs'
    code_changed = true
    klass = path[%r{\Aapp/src/androidTest/java/(.+Test)\.kt\z}, 1]&.tr('/', '.')
    if klass
      owners = lanes.select do |lane|
        assigned?(lane, klass) || lane.fetch('tests', '').split(',').any? { |test| test.start_with?(klass + '#') }
      end
      return [lanes, "Unassigned test #{path}: full coverage"] if owners.empty?
      selected.concat(owners)
    elsif (pattern = DOMAIN_LANES[domain(path)])
      selected.concat(lanes.select { |lane| lane.fetch('case').match?(pattern) })
    else
      return [lanes, "Shared or unrecognized path #{path}: full coverage"]
    end
  end
  return [[], 'Documentation-only: emulator lanes omitted; build/JVM/lint still required'] unless code_changed
  selected.concat(lanes.select { |lane| BASELINE.include?(lane.fetch('case')) })
  [lanes.select { |lane| selected.include?(lane) }, 'Affected features plus compatibility/persistence/upgrade/widget baseline']
end

def changed_paths(base, head, directory = '.')
  raise 'Missing or invalid commit SHA' unless [base, head].all? { |sha| sha.to_s.match?(/\A[0-9a-f]{40,64}\z/) }
  common, error, status = Open3.capture3('git', '-C', directory, 'merge-base', base, head)
  raise "Merge base unavailable: #{error}" unless status.success?
  # Report both old and new paths for renames, and retain deleted paths.
  paths, error, status = Open3.capture3('git', '-C', directory, 'diff', '--name-only', '--no-renames', '-z', common.strip, head, '--')
  raise "Diff unavailable: #{error}" unless status.success?
  paths.split("\0")
end

def self_test(lanes)
  require 'tmpdir'
  check = ->(condition, label) { raise label unless condition }
  main = 'app/src/main/java/atlow/chemi/mymada/'
  oxygen, = select_lanes(lanes, [main + 'oxygen/OxygenCalculation.kt'])
  check.call(oxygen.size < lanes.size, 'Feature changes must reduce lanes')
  check.call(oxygen.any? { |lane| lane['case'] == 'oxygen-hebrew' }, 'Hebrew variant lost')
  BASELINE.each { |name| check.call(oxygen.any? { |lane| lane['case'] == name }, "Baseline lost: #{name}") }
  check.call(oxygen.map { |lane| lane['api'] }.uniq.sort == [24, 37], 'API variants lost')
  calls, = select_lanes(lanes, [main + 'recieversAndServices/GrobootRec.kt'])
  %w[notification-grant notification-deny notification-dismiss notification-settings listener-native listener-rn listener-both overlay-allowed overlay-denied external-audio].each do |name|
    check.call(calls.any? { |lane| lane['case'] == name }, "Call variant lost: #{name}")
  end
  vitals, = select_lanes(lanes, [main + 'tools/vitals/VitalsScreen.kt'])
  %w[vitals-alone vitals-aosp vitals-google-4k].each do |name|
    check.call(vitals.any? { |lane| lane['case'] == name }, "Image variant lost: #{name}")
  end
  check.call(vitals.any? { |lane| lane['case'] == 'home-and-integrations' }, 'Medical integration consumer lost')
  docs, = select_lanes(lanes, ['docs/TESTING.md', 'README.md'])
  check.call(docs.empty?, 'Docs-only must omit emulators')
  mixed, = select_lanes(lanes, ['README.md', main + 'oxygen/OxygenScreen.kt'])
  check.call(mixed == oxygen, 'Docs must not hide code changes')
  %w[app/build.gradle .github/android-lanes.yml .github/scripts/emulator-smoke.sh app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml unknown.txt].each do |path|
    check.call(select_lanes(lanes, [path]).first == lanes, "Unsafe fallback: #{path}")
  end
  check.call(select_lanes(lanes, []).first == lanes, 'Empty diff must run full coverage')
  check.call(select_lanes(lanes, nil).first == lanes, 'Missing diff must run full coverage')
  check.call(select_lanes(lanes, ['README.md'], full_reason: 'manual').first == lanes, 'Manual full override')
  test = 'app/src/androidTest/java/atlow/chemi/mymada/calls/NotificationPermissionTest.kt'
  check.call(select_lanes(lanes, [test]).first.count { |lane| lane['case'].start_with?('notification-') } == 4, 'Method-selected test variants lost')
  unknown = 'app/src/androidTest/java/atlow/chemi/mymada/NewTest.kt'
  check.call(select_lanes(lanes, [unknown]).first == lanes, 'Unassigned test must run full coverage')
  expanded = lanes + [{'api' => 37, 'case' => 'new-unmapped-lane'}]
  check.call(select_lanes(expanded, [main + 'oxygen/OxygenScreen.kt']).first == expanded, 'New lanes must not silently disappear')

  Dir.mktmpdir('lane-diff') do |dir|
    git = lambda do |*args|
      out, err, status = Open3.capture3('git', '-C', dir, '-c', 'user.name=CI test', '-c', 'user.email=ci@example.invalid', *args)
      raise err unless status.success?
      out.strip
    end
    git.call('init', '-q')
    File.write(File.join(dir, 'old name.kt'), 'fixture')
    git.call('add', '.')
    git.call('commit', '-qm', 'base')
    base = git.call('rev-parse', 'HEAD')
    File.rename(File.join(dir, 'old name.kt'), File.join(dir, 'new name.kt'))
    git.call('add', '-A')
    git.call('commit', '-qm', 'rename')
    head = git.call('rev-parse', 'HEAD')
    check.call(changed_paths(base, head, dir).sort == ['new name.kt', 'old name.kt'], 'Rename paths lost')
    git.call('checkout', '-qb', 'base-moved', base)
    File.write(File.join(dir, 'base-only.kt'), 'base change')
    git.call('add', '.')
    git.call('commit', '-qm', 'base moved')
    check.call(changed_paths(git.call('rev-parse', 'HEAD'), head, dir).sort == ['new name.kt', 'old name.kt'], 'PR merge-base comparison changed')
  end

  gate = YAML.load_file('.github/workflows/android-build.yml').dig('jobs', 'required', 'steps', 0, 'run')
  [
    ['success', 'success', 'true', true], ['success', 'skipped', 'false', true],
    ['success', 'failure', 'true', false], ['failure', 'skipped', 'true', false],
    ['success', 'cancelled', 'true', false], ['success', 'skipped', 'true', false],
    ['cancelled', 'skipped', '', false], ['success', 'skipped', '', false]
  ].each do |build, emulator, has_lanes, expected|
    _, _, status = Open3.capture3({'BUILD_RESULT' => build, 'EMULATOR_RESULT' => emulator, 'HAS_LANES' => has_lanes}, 'bash', '-e', '-c', gate)
    check.call(status.success? == expected, "Required-check gate: #{[build, emulator, has_lanes]}")
  end
  puts "Affected-lane selection, git diff and required-check tests passed (#{lanes.size} canonical lanes)"
end

if $PROGRAM_NAME == __FILE__
  lanes = YAML.load_file('.github/android-lanes.yml').fetch('include')
  if ARGV == ['--self-test']
    self_test(lanes)
    exit
  end
  abort 'Usage: select-android-lanes.rb [--self-test|--paths|--full]' unless [[], ['--paths'], ['--full']].include?(ARGV)
  paths = nil
  base = head = nil
  full_reason = nil
  if ARGV == ['--paths']
    paths = STDIN.read.split("\0")
  elsif ARGV == ['--full'] || ENV['GITHUB_EVENT_NAME'] != 'pull_request'
    full_reason = 'Non-PR run: full coverage (manual, master push or weekly schedule)'
  else
    begin
      event = JSON.parse(File.read(ENV.fetch('GITHUB_EVENT_PATH')))
      base = event.fetch('pull_request').fetch('base').fetch('sha')
      head = event.fetch('pull_request').fetch('head').fetch('sha')
      paths = changed_paths(base, head)
    rescue StandardError => error
      full_reason = "Cannot determine PR diff: full coverage (#{error.message})"
    end
  end
  selected, reason = select_lanes(lanes, paths, full_reason: full_reason)
  if ENV['GITHUB_EVENT_NAME'] == 'workflow_dispatch' && ENV['CACHE_ONLY'] == 'true'
    selected = []
    reason = 'Explicit cache-only dispatch: not an instrumentation acceptance run'
  end
  names = ->(items) { items.map { |lane| "api#{lane.fetch('api')}/#{lane.fetch('case')}" } }
  report = {base: base, head: head, reason: reason, paths: paths, selected: names.call(selected), omitted: names.call(lanes - selected)}
  File.write('lane-selection.json', JSON.pretty_generate(report) + "\n")
  puts JSON.pretty_generate(report)
  if ENV['GITHUB_OUTPUT']
    File.open(ENV.fetch('GITHUB_OUTPUT'), 'a') do |file|
      file.puts "matrix=#{JSON.generate({'include' => selected})}"
      file.puts "has_lanes=#{!selected.empty?}"
    end
  end
  if ENV['GITHUB_STEP_SUMMARY']
    File.open(ENV.fetch('GITHUB_STEP_SUMMARY'), 'a') do |file|
      file.puts "## Emulator lane selection\n\n#{selected.size}/#{lanes.size} lanes selected."
      # JSON quoting keeps filenames/reasons readable without treating them as markup.
      file.puts "\n```json\n#{JSON.pretty_generate(report)}\n```"
      file.puts "\nOmitted lanes are not test passes. Full coverage: dispatch Android build with cache_only=false."
    end
  end
end
