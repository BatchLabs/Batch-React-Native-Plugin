import { ConfigPlugin, withMainActivity } from '@expo/config-plugins';

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

export const modifyMainKotlinActivity = (content: string): string => {
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

    newContent =
      start +
      `\n  override fun onNewIntent(intent: Intent?) {
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

export const modifyMainActivity = (content: string): string => {
  return isKotlinMainActivity(content) ? modifyMainKotlinActivity(content) : modifyMainJavaActivity(content);
};

const isKotlinMainActivity = (content: string): boolean => {
  return content.includes('class MainActivity : ReactActivity()');
};

export const withReactNativeBatchMainActivity: ConfigPlugin<object | void> = config => {
  return withMainActivity(config, config => {
    return {
      ...config,
      modResults: {
        ...config.modResults,
        contents: modifyMainActivity(config.modResults.contents),
      },
    };
  });
};
