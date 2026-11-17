require 'bundler/setup'
require 'minitest/autorun'
require 'cocoapods'
require 'addressable/uri'
require 'xcodeproj'
require 'tmpdir'

class RubyDependenciesTest < Minitest::Test
  def test_loaded_gems_match_the_lockfile
    locked = Bundler.locked_gems.specs.to_h { |spec| [spec.name, spec.version] }

    %w[addressable cocoapods cocoapods-core xcodeproj].each do |name|
      assert_equal locked.fetch(name), Gem.loaded_specs.fetch(name).version, name
    end
  end

  def test_cocoapods_parses_a_podfile
    podfile = Pod::Podfile.from_ruby('Podfile', <<~RUBY)
      platform :ios, '15.1'
      target 'DependencySmoke' do
        pod 'DependencySmokeSDK'
      end
    RUBY

    assert_equal 'DependencySmoke', podfile.target_definitions.fetch('DependencySmoke').name
    assert_equal ['DependencySmokeSDK'], podfile.dependencies.map(&:name)
  end

  def test_addressable_normalizes_a_download_url
    uri = Addressable::URI.parse('HTTPS://EXAMPLE.COM:443/sdk/../sdk/offerwall%20archive.zip')

    assert_equal 'https://example.com/sdk/offerwall%20archive.zip', uri.normalize.to_s
  end

  def test_xcodeproj_writes_and_reads_an_ios_project
    Dir.mktmpdir('adpopcorn-ruby-test-') do |directory|
      project_path = File.join(directory, 'DependencySmoke.xcodeproj')
      project = Xcodeproj::Project.new(project_path)
      project.new_target(:application, 'DependencySmoke', :ios, '15.1')
      project.save

      reopened = Xcodeproj::Project.open(project_path)
      assert_equal ['DependencySmoke'], reopened.targets.map(&:name)
    end
  end
end
