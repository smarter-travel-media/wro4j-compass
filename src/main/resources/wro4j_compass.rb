if ENV['WRO_GEM_HOME'] == nil
  ENV['WRO_GEM_HOME'] = "/tmp/gems"
end

ENV['GEM_HOME'] = ENV['WRO_GEM_HOME']

puts "Gem home: #{ENV['GEM_HOME']}"

require 'rubygems'
require 'rubygems/dependency_installer'

required_gems = Array[['sass','3.2.12'],['compass', '0.12.2'],['compass-rails','1.1.2'], ['susy','1.0.9']]
if !File.exist?(ENV['GEM_HOME'])
  gem_installer = Gem::DependencyInstaller.new
  puts "GEM_HOME does not exist. Installing gems"
  required_gems.each{|required_gem|
    gem_installer.install(required_gem[0],required_gem[1])
  }
else
  puts "GEM_HOME already exists, skipping gem installation"
end

require 'compass'
require 'compass/commands'
require 'sass'
require 'sass/plugin'
require 'susy'



module Compass

  class Compiler

    def compile_string(str, sass_file_name)
       puts "Compiling #{sass_file_name}"
       start_time = end_time = nil
        css_content = logger.red do
          timed do
            engine_string(str, sass_file_name).render
          end
        end
        duration = options[:time] ? "(#{(css_content.__duration * 1000).round / 1000.0}s)" : ""
       css_content
    end

    def engine_string(str, sass_file_name)
      syntax = (sass_file_name =~ /\.(s[ac]ss)$/) && $1.to_sym || :sass
      opts = sass_options.merge(:filename => sass_file_name, :syntax => syntax)
      Sass::Engine.new(str, opts)
    end
  end

end

class CompassCompiler
    include Java::no.bekk.wro4j.compass.CompassCompiler

  def initialize
    puts "Creating compiler"
  end

  def compile(compass_dir, content, real_file_name)
    cmd = Compass::Commands::UpdateProject.new(compass_dir, {:sass_files => [real_file_name]})
    compiler = cmd.new_compiler_instance
    compiler.compile_string(content, real_file_name)
  end
end

CompassCompiler.new