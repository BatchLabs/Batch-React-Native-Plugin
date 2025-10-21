import { ConfigPlugin, ExportedConfigWithProps, withMainActivity } from '@expo/config-plugins';
import { ApplicationProjectFile } from '@expo/config-plugins/build/android/Paths';

export type MainActivityProps = {
  shouldUseNonNullableIntent?: boolean;
};

export const modifyMainJavaActivity = (content: string): string => {
  let newContent = content;

  if (!newContent.includes('import android.content.Intent;')) {
    newContent = content.replace(
      'import com.facebook.react.ReactActivity;',
      `import com.facebook.react.ReactActivity;
import android.content.Intent;
import com.batch.android.Batch;`
    );
  } else {
    newContent = content.replace(
      'import com.facebook.react.ReactActivity;',
      `import com.facebook.react.ReactActivity;
      import com.batch.android.Batch;`
    );
  }

  if (!newContent.includes('onNewIntent(')) {
    const lastBracketIndex = newContent.lastIndexOf('}');

    const start = newContent.substring(0, lastBracketIndex);
    const end = newContent.substring(lastBracketIndex);

    newContent =
      start +
      `\n  @Override
  public void onNewIntent(Intent intent)
  {
      super.onNewIntent(intent);
      Batch.onNewIntent(this, intent);
  }\n\n` +
      end;
  } else {
    newContent = newContent.replace(
      'super.onNewIntent(intent);',
      `super.onNewIntent(intent);
    Batch.onNewIntent(this, intent);`
    );
  }

  return newContent;
};

export const modifyMainKotlinActivity = (content: string, useNonNullableIntent: boolean): string => {
  let newContent = content;

  if (!newContent.includes('import android.content.Intent')) {
    newContent = content.replace(
      'import com.facebook.react.defaults.DefaultReactActivityDelegate',
      `import com.facebook.react.defaults.DefaultReactActivityDelegate
import android.content.Intent
import com.batch.android.Batch`
    );
  } else {
    newContent = content.replace(
      'import com.facebook.react.defaults.DefaultReactActivityDelegate',
      `import com.facebook.react.defaults.DefaultReactActivityDelegate
      import com.batch.android.Batch`
    );
  }

  if (!newContent.includes('onNewIntent(')) {
    const lastBracketIndex = newContent.lastIndexOf('}');

    const start = newContent.substring(0, lastBracketIndex);
    const end = newContent.substring(lastBracketIndex);

    // Use non-nullable Intent for SDK 54+, nullable for SDK 53 and below
    const intentType = useNonNullableIntent ? 'Intent' : 'Intent?';

    newContent =
      start +
      `\n  override fun onNewIntent(intent: ${intentType}) {
        super.onNewIntent(intent)
        Batch.onNewIntent(this, intent)
  }\n` +
      end;
  } else {
    newContent = newContent.replace(
      'super.onNewIntent(intent);',
      `Batch.onNewIntent(this, intent);
    super.onNewIntent(intent);`
    );
  }

  return newContent;
};

export const modifyMainActivity = (
  config: ExportedConfigWithProps<ApplicationProjectFile>,
  shouldUseNonNullableIntent: boolean = false
): string => {
  return isKotlinMainActivity(config.modResults.contents)
    ? modifyMainKotlinActivity(config.modResults.contents, shouldUseNonNullableIntent)
    : modifyMainJavaActivity(config.modResults.contents);
};

const isKotlinMainActivity = (content: string): boolean => {
  return content.includes('class MainActivity : ReactActivity()');
};

export const withReactNativeBatchMainActivity: ConfigPlugin<MainActivityProps | void> = (config, props) => {
  const shouldUseNonNullableIntent = props?.shouldUseNonNullableIntent ?? false;

  return withMainActivity(config, config => {
    return {
      ...config,
      modResults: {
        ...config.modResults,
        contents: modifyMainActivity(config, shouldUseNonNullableIntent),
      },
    };
  });
};
