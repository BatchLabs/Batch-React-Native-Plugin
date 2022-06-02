## How to develop

### Install dependcies

Install the plugin's dependencies.

`yarn install`

### Link your local package

Link the plugin locally

`yarn link`

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

And that's it, you'r are ready to develop.
