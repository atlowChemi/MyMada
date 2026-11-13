#!/usr/bin/env ruby
require 'yaml'

def assigned?(job, test)
  klass = test.split('#').first
  excluded = job.fetch('exclude', '').split(',')
  return false if excluded.include?(klass) || excluded.include?(test)
  job.fetch('tests', '').split(',').any? { |selector| selector == klass || selector == test } ||
    job.fetch('packages', '').split(',').any? { |package| klass.start_with?(package + '.') }
end

if ARGV == ['--self-test']
  test = 'example.calls.RouteTest#delivery'
  raise 'package selection' unless assigned?({'packages' => 'example.calls'}, test)
  raise 'package boundary' if assigned?({'packages' => 'example.call'}, test)
  raise 'class exclusion' if assigned?({'packages' => 'example.calls', 'exclude' => 'example.calls.RouteTest'}, test)
  raise 'method selection' unless assigned?({'tests' => test}, test)
  raise 'sibling method leakage' if assigned?({'tests' => 'example.calls.RouteTest#other'}, test)
  raise 'unassigned test' if assigned?({'tests' => 'example.OtherTest'}, test)
  puts 'Assignment selection checks passed'
  exit
end
abort 'Usage: check-test-assignments.rb [--self-test]' unless ARGV.empty?

jobs = YAML.load_file('.github/workflows/android-build.yml').fetch('jobs')
  .fetch('emulator').fetch('strategy').fetch('matrix').fetch('include')
tests = Dir['app/src/androidTest/java/**/*.kt'].flat_map do |path|
  source = File.read(path)
  annotations = source.scan(/^\s*@(?:org\.junit\.)?Test\b/).length
  next [] if annotations.zero?
  methods = source.scan(/@(?:org\.junit\.)?Test\s+fun\s+(\w+)\s*\(/).flatten
  abort "Unsupported test declaration in #{path}; update the inventory parser" unless methods.length == annotations
  package = source[/^package\s+([\w.]+)/, 1]
  klass = File.basename(path, '.kt')
  abort "Unsupported test class in #{path}" unless package && source.match?(/\bclass\s+#{Regexp.escape(klass)}\b/)
  methods.map { |method| "#{package}.#{klass}##{method}" }
end
abort 'No instrumentation tests discovered' if tests.empty?
errors = []
jobs.each do |job|
  job.fetch('tests', '').split(',').each do |selector|
    unless tests.any? { |test| test == selector || test.split('#').first == selector }
      errors << "Unknown selector #{selector} in #{job['case']}"
    end
  end
end
tests.each do |test|
  owners = jobs.select { |job| assigned?(job, test) }
  errors << "Unassigned: #{test}" if owners.empty?
  puts "#{test}: #{owners.map { |job| "api#{job['api']}/#{job['case']}" }.join(', ')}"
end

# Required state variants cannot be satisfied by assigning the class once.
root = 'atlow.chemi.mymada.'
variants = []
%w[grant deny dismiss settings].each do |route|
  variants << [root + 'calls.NotificationPermissionTest#freshInstallPermissionRoute', 37, {'permission-route' => route}]
end
%w[grant deny].each do |route|
  variants << [root + 'MetronomePermissionTest#recreatedPromptStartsOnceAfterActualOsDecisionAndStillStops', 37, {'permission-route' => route}]
end
%w[true false].each do |grant|
  variants << [root + 'MetronomeNotificationTest#startStopAndNotificationControls', 37, {'grant-notifications' => grant}]
end
[24, 37].each do |api|
  variants << [root + 'settings.ExternalAudioDispatchTest#selectedExternalAudioDecodesAndDispatchesOnAlarmStream', api, {'external-audio' => true}]
  variants << [root + 'MetronomeCallVisualTest#captureProductionFixtures', api, {'language' => nil}]
  %w[native rn both].each do |listener|
    %w[calls.ListenerAccessTest#osBoundListenerPreservesRepeatedSourceNotifications calls.SummaryNotificationTest#osSummariesDoNotReplaceCallsButMeaningfulSummariesAndRepeatsStillAlert].each do |test|
      variants << [root + test, api, {'listeners' => listener, 'call-source' => true, 'overlay' => 'allow'}]
    end
  end
  tests.grep(/\.OverlayLifecycleTest#/).each do |test|
    variants << [test, api, {'overlay' => test.end_with?('#deniedOverlayStopsWithoutWindows') ? 'deny' : 'allow'}]
  end
  tests.grep(/\.MonitorLifecycleTest#/).each { |test| variants << [test, api, {'listeners' => 'native'}] }
  variants << [root + 'oxygen.OxygenTest#editsTankAndRecreation', api, {'language' => 'he'}]
end
variants.each do |test, api, fields|
  unless jobs.any? { |job| job['api'] == api && assigned?(job, test) && fields.all? { |key, value| job[key] == value } }
    errors << "Missing variant: api#{api} #{test} #{fields}"
  end
end
abort errors.join("\n") unless errors.empty?
puts "Assigned #{tests.length} instrumentation methods and #{variants.length} required state/API variants across #{jobs.length} jobs. Runtime skips/passes require JUnit review."
