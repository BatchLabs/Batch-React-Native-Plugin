Pod::Spec.new do |s|
  s.name         = "RNBatchPush"
  s.version      = "1.0.0"
  s.summary      = "Batch.com React-Native Plugin"
  s.homepage     = "https://github.com/BatchLabs/Batch-React-Native-Plugin"
  s.license      = { :type => "MIT", :file => "./LICENSE" }
  s.authors = {
    "Batch.com" => "support@batch.com"
  }
  s.platform     = :ios, "10.0"
  s.source       = { :git => "git@github.com:BatchLabs/Batch-React-Native-Plugin.git", :tag => "master" }
  s.source_files  = "ios/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency 'Batch', '~> 1.19.0'
end