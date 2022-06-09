## How to develop

### Install dependcies

Install the plugin's dependencies.

`yarn install`

### Link your local package

Link the plugin locally

`yarn link`

Make typescript compile files on save

`tsc -w`

Then go to your test app root folder and link the plugin.

`yarn link "@batch.com/react-native-plugin"`

So that Metro can detect modifications outside your node modules, update your app's `metro-config.js` as following :

```js
const path = require('path');
const batchPath = '../plugin/directory/path';

module.exports = {
    ...
    watchFolders: [path.resolve(__dirname, batchPath)],
};
```

Do not forget to install pod dependencies if you want to run on ios :

```bash
cd ios
pod install
```

If you had already a metro server instance running, please kill it and start again using `--reset-cache` option.

If it's not working, maybe try using wml.

### Use wml

If linking your local module isn't working, you can use `wml` to watch your package folder and update modification to your node_modules.

First, install your local module in your test app

`yarn add "../plugin/directory/path"`

Install wml

`npm install -g wml`

Add the link to wml

`wml add ../plugin/directory/path ./your_test_app/node_modules/@batch.com/react-native-plugin`

Start watching

`wml start`

Make typescript compile files on save

`tsc -w`



