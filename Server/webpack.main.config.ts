import { Configuration } from 'webpack'
import { rules } from './webpack.rules'


import relocateLoader from '@vercel/webpack-asset-relocator-loader'

export const mainWebpackConfig: Configuration = {

    entry: './src/main.ts',

    module: {
        rules,
        
    },
    resolve: {
        extensions: ['.js', '.ts', '.jsx', '.tsx', '.css', '.json'],
      },

    plugins: [
        {
            apply(compiler) {
              compiler.hooks.compilation.tap('webpack-asset-relocator-loader', (compilation) => {
                relocateLoader.initAssetCache(compilation, 'native_modules');
              });
            },
          },
    ]
}
