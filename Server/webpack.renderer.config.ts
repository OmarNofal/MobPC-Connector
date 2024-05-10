import { Configuration } from 'webpack'
import { rules } from './webpack.rules'



export const rendererWebpackConfig: Configuration = {

    module: {
        rules
    },

    resolve: {
        extensions: ['.js', '.ts', '.jsx', '.tsx', '.css', '.json'],
      },

}