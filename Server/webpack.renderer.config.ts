import { Configuration } from 'webpack'
import { rules } from './webpack.rules'
import ForkTsCheckerWebpackPlugin from 'fork-ts-checker-webpack-plugin'

export const rendererWebpackConfig: Configuration = {
    module: {
        rules,
    },

    resolve: {
        extensions: ['.js', '.ts', '.jsx', '.tsx', '.css', '.json'],
    },

    plugins: [new ForkTsCheckerWebpackPlugin()],

    watchOptions: {
        ignored: /node_modules/,
    },
}
