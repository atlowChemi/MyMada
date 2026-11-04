# Run only against an owned, network-isolated emulator with production12822 and candidate installed.
# Usage: ruby docs/checks/tools-shell-parity.rb emulator-5640 /absolute/evidence/directory
require 'open3'
require 'fileutils'

serial, output = ARGV
abort('Pass an owned emulator serial and evidence directory') unless serial&.match?(/\Aemulator-\d+\z/) && output
FileUtils.mkdir_p(output)
adb = File.join(ENV.fetch('ANDROID_HOME'), 'platform-tools/adb')
command = lambda do |*args|
  data, status = Open3.capture2e(adb, '-s', serial, *args)
  abort(data) unless status.success?
  data
end
api = command.call('shell', 'getprop', 'ro.build.version.sdk').to_i
density = command.call('shell', 'wm', 'density').scan(/\d+/).last.to_f / 160
screen = lambda do
  image, status = Open3.capture2(adb, '-s', serial, 'exec-out', 'screencap', '-p', binmode: true)
  abort('Screenshot failed') unless status.success? && image.start_with?("\x89PNG".b)
  image
end
ENV.fetch('PACKAGES', 'atlow.chemi.mymada atlow.chemi.mymada.debug').split.each do |package|
  label = ENV.fetch('LABEL', package.end_with?('.debug') ? 'candidate' : 'production12822')
  %w[en he].product([1.0, 1.5], [0, 1], %w[UtillitiesAndTools SettingsPage]).each do |language, font, rotation, activity|
    name = "#{label}-#{activity}-#{language}-font#{font}-rotation#{rotation}"
    next if ENV['CASE'] && !name.include?(ENV['CASE'])
    if api == 24 && package == 'atlow.chemi.mymada'
      configuration = command.call('shell', 'am', 'get-config').lines.first.to_s
      expected = language == 'he' ? /config: (?:mcc\d+-mnc\d+-)?(iw|he)-rIL/ : /config: (?:mcc\d+-mnc\d+-)?en-rUS/
      abort("Set the owned emulator's system language to #{language} in Android Settings first; use CASE=-#{language}- to capture one locale. #{configuration}") unless configuration.match?(expected)
    end
    command.call('shell', 'am', 'force-stop', package)
    command.call('shell', 'settings', 'put', 'system', 'font_scale', font.to_s)
    if api < 29
      command.call('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
      command.call('shell', 'settings', 'put', 'system', 'user_rotation', rotation.to_s)
    end
    preferences = File.join(output, 'fixture.xml')
    File.write(preferences, "<?xml version='1.0' encoding='utf-8' standalone='yes' ?><map><string name='lng'>#{language}</string><boolean name='Mapp' value='false'/><boolean name='WasLangChanged' value='false'/></map>")
    command.call('push', preferences, '/data/local/tmp/tools-shell-prefs.xml')
    command.call('shell', "P=/data/data/#{package}; U=$(stat -c %u $P); mkdir -p $P/shared_prefs; cp /data/local/tmp/tools-shell-prefs.xml $P/shared_prefs/Settings.xml; chown -R $U:$U $P/shared_prefs; chmod 660 $P/shared_prefs/Settings.xml; restorecon -R $P/shared_prefs")
    command.call('shell', 'cmd', 'locale', 'set-app-locales', package, '--locales', language) if api >= 33
    started = command.call('shell', 'am', 'start', '-W', '-n', "#{package}/atlow.chemi.mymada.#{activity}")
    abort(started) unless started.include?('Status: ok')
    command.call('shell', 'wm', 'user-rotation', 'lock', rotation.to_s) if api >= 29
    image = nil
    6.times do
      command.call('shell', 'wm', 'user-rotation', 'lock', rotation.to_s) if api >= 29
      sleep 0.5 # Let the display rotation finish before opening the drawer.
      image = screen.call
      width, height = image.byteslice(16, 8).unpack('NN')
      break if (width > height) == (rotation == 1)
    end
    width, height = image.byteslice(16, 8).unpack('NN')
    abort("Unexpected display rotation for #{name}") unless (width > height) == (rotation == 1)
    sleep 2 # Matching display dimensions can precede Activity/Compose recreation.
    # Pixel 2 emulator: toolbar navigation is 28dp from start in both orientations.
    # Native/Compose metronome emits periodic accessibility events, so uiautomator's
    # idle-based dump is not a reliable prerequisite for capturing this shell.
    half_toolbar = rotation == 1 ? 24 : 28
    gestural = command.call('shell', 'settings', 'get', 'secure', 'navigation_mode').strip == '2'
    right_inset = rotation == 1 && !gestural ? 48 : 0
    x = language == 'he' ? width - (right_inset + 28) * density : 28 * density
    y = (24 + half_toolbar) * density
    command.call('shell', 'input', 'tap', x.round.to_s, y.round.to_s)
    sleep 4 # Let the drawer spring, ripple and API24 native scrollbar fade settle.
    image = screen.call
    File.binwrite(File.join(output, "#{name}.png"), image)
    File.write(File.join(output, "#{name}.capture.txt"), "#{package} #{language} font=#{font} rotation=#{rotation} size=#{width}x#{height} tap=#{x.round},#{y.round}\n")
    puts name
  end
end
command.call('shell', 'settings', 'put', 'system', 'font_scale', '1.0')
command.call('shell', 'settings', 'put', 'system', 'user_rotation', '0')
command.call('shell', 'wm', 'user-rotation', 'lock', '0') if api >= 29
