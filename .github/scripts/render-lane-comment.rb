#!/usr/bin/env ruby
require 'json'

MARKER = '<!-- android-lane-selection -->'

def render(report, run_url)
  # Mermaid entity encoding prevents filenames from introducing graph syntax,
  # Markdown fences, HTML, or Mermaid directives.
  label = ->(text) { text.to_s.gsub(/[^a-zA-Z0-9 .\/:_-]/) { |char| "##{char.ord};" } }
  selected = report.fetch('selected')
  ids = selected.each_with_index.to_h { |name, index| [name, "l#{index}"] }
  graph = ['flowchart LR', '  changes["PR changes"]']
  selected.each { |name| graph << "  #{ids.fetch(name)}[\"#{label.call(name)}\"]" }
  baseline = report.fetch('baseline')
  unless baseline.empty?
    graph << '  changes --> smoke["Required smoke baseline"]'
    baseline.each { |name| graph << "  smoke --> #{ids.fetch(name)}" }
  end
  causes = report.fetch('causes')
  causes.each_with_index do |cause, index|
    graph << "  f#{index}[\"#{label.call(cause.fetch('path'))}\"]"
    graph << "  changes --> f#{index}"
    if cause.fetch('full')
      graph << "  f#{index} --> full[\"Full-suite fallback\"]"
    elsif cause.fetch('lanes').empty?
      graph << "  f#{index} --> docs[\"No detailed emulator lanes\"]"
    else
      cause.fetch('lanes').each { |name| graph << "  f#{index} --> #{ids.fetch(name)}" }
    end
  end
  if causes.any? { |cause| cause.fetch('full') } || (causes.empty? && !selected.empty?)
    graph << '  changes --> full["Full-suite fallback"]' if causes.empty?
    selected.each { |name| graph << "  full --> #{ids.fetch(name)}" }
  end
  body = <<~MARKDOWN
    #{MARKER}
    ## Android emulator selection
    **#{selected.size} selected; #{report.fetch('omitted').size} omitted.** “Dirty” means selected for testing, not failed.

    Commit: `#{label.call(report.fetch('head'))}` · [Workflow run](#{run_url})

    #{label.call(report.fetch('reason'))}

    ```mermaid
    #{graph.join("\n")}
    ```

    Arrows show file → detailed lanes; smoke is added independently. Omitted lanes are not passes.
    Full paths, selection reasons and omissions are in the run’s **Emulator lane selection** summary and **lane-selection** artifact.
  MARKDOWN
  # GitHub caps comment bodies; the artifact retains the complete mapping.
  if body.bytesize > 60_000
    body = "#{MARKER}\n## Android emulator selection\n#{selected.size} selected; #{report.fetch('omitted').size} omitted.\nThe graph exceeds GitHub’s comment limit. See the [workflow run](#{run_url}) and its lane-selection artifact for the complete file-to-lane mapping.\n"
  end
  body
end

if ARGV == ['--self-test']
  fixture = {
    'head' => 'a' * 40, 'reason' => 'Affected', 'selected' => %w[api24/platform-alone api24/oxygen],
    'omitted' => ['api24/calls'], 'baseline' => ['api24/platform-alone'],
    'causes' => [{'path' => "bad\"]\n```<script>.kt", 'full' => false, 'lanes' => ['api24/oxygen']}]
  }
  body = render(fixture, 'https://github.com/example/repo/actions/runs/1')
  raise 'File attribution lost' unless body.include?('f0 --> l1') && body.include?('smoke --> l0')
  raise 'Unsafe filename' if body.include?('<script>') || body.include?("```<")
  fixture['causes'][0]['full'] = true
  raise 'Fallback lost' unless render(fixture, '').include?('full --> l1')
  fixture.merge!('selected' => [], 'baseline' => [], 'causes' => [{'path' => 'README.md', 'full' => false, 'lanes' => []}])
  raise 'Docs-only lost' unless render(fixture, '').include?('No detailed emulator lanes')
  puts 'Lane comment attribution, fallback, docs-only and escaping checks passed'
else
  abort 'Usage: render-lane-comment.rb REPORT RUN_URL' unless ARGV.size == 2
  puts render(JSON.parse(File.read(ARGV[0])), ARGV[1])
end
