Pod::Spec.new do |s|
  s.name         = "RNBatchPush"
  s.version      = "1.0.0"
  s.summary      = "Batch.com React-Native Plugin"
  s.homepage     = "https://github.com/BatchLabs/Batch-React-Native-Plugin"
  s.license      = { :type => "MIT", :file => "./LICENSE" }
  s.authors = {
    "Batch.com" => "support@batch.com"
  }
  s.platform     = :ios, "15.0"
  s.source       = { :git => "git@github.com:BatchLabs/Batch-React-Native-Plugin.git", :tag => "master" }
  s.source_files  = "*.{h,m,mm,swift}"
  s.requires_arc = true

  install_modules_dependencies(s)

  s.dependency "React"
  s.dependency 'Batch', '~> 3.1.0'
  s.dependency 'ExpoModulesCore'

  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }
end
