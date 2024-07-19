import { ConfigPlugin, withMainApplication } from '@expo/config-plugins';

export const modifyMainJavaApplication = (content: string): string => {
  let newContent = content;
  if (newContent.includes('import com.facebook.react.PackageList;')) {
    newContent = content.replace(
      'import com.facebook.react.PackageList;',
      `import com.batch.batch_rn.RNBatchModule;
import com.facebook.react.PackageList;`
    );
  }
  if (newContent.includes('ApplicationLifecycleDispatcher.onApplicationCreate(this)')) {
    newContent = newContent.replace(
      'ApplicationLifecycleDispatcher.onApplicationCreate(this);',
      `ApplicationLifecycleDispatcher.onApplicationCreate(this);
    RNBatchModule.initialize(this);`
    );
  }
  return newContent;
};

export const modifyMainKotlinApplication = (content: string): string => {
  let newContent = content;
  if (newContent.includes('import com.facebook.react.PackageList')) {
    newContent = content.replace(
      'import com.facebook.react.PackageList',
      `import com.batch.batch_rn.RNBatchModule
import com.facebook.react.PackageList`
    );
  }
  if (newContent.includes('ApplicationLifecycleDispatcher.onApplicationCreate(this)')) {
    newContent = newContent.replace(
      'ApplicationLifecycleDispatcher.onApplicationCreate(this)',
      `ApplicationLifecycleDispatcher.onApplicationCreate(this)
    RNBatchModule.initialize(this)`
    );
  }
  return newContent;
};

export const modifyMainApplication = (content: string): string => {
  return isKotlinMainApplication(content) ? modifyMainKotlinApplication(content) : modifyMainJavaApplication(content);
};

const isKotlinMainApplication = (content: string): boolean => {
  return content.includes('class MainApplication : Application(), ReactApplication');
};

export const withReactNativeBatchMainApplication: ConfigPlugin<object | void> = config => {
  return withMainApplication(config, config => {
    return {
      ...config,
      modResults: {
        ...config.modResults,
        contents: modifyMainApplication(config.modResults.contents),
      },
    };
  });
};
