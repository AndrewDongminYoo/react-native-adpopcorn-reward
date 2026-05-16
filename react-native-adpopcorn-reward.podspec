require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-adpopcorn-reward"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.author       = package["author"]
  s.platform     = :ios, "12.0"
  s.source       = { :git => package["repository"]["url"], :tag => s.version }
  s.source_files = "ios/**/*.{h,m,mm}"
  s.dependency   "React-Core"
  s.dependency   "AdPopcornOfferwall", "5.2.3"
end
