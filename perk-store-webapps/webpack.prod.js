const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

const config = merge(webpackCommonConfig, {
  mode: 'production',
  entry: {
    perkstore: './src/main/webapp/vue-app/perk-store.js',
    perkstoreOrder: './src/main/webapp/vue-app/perk-store-order.js',
    perkstoreSearch: './src/main/webapp/vue-app/perk-store-search.js'
  },
  output: {
    path: path.join(__dirname, 'target/perk-store/'),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  externals: {
    vue: 'Vue',
    vuetify: 'Vuetify',
    jquery: '$'
  }
});

module.exports = config;
