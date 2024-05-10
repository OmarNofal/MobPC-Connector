import { Configuration } from 'webpack'
import { rules } from './webpack.rules'


export const mainWebpackConfig: Configuration = {

    entry: './src/main.ts',

    module: {
        rules,
        
    },
    resolve: {
        extensions: ['.js', '.ts', '.jsx', '.tsx', '.css', '.json'],
      },
}
