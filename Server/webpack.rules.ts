import { ModuleOptions } from 'webpack'

export const rules: Required<ModuleOptions>['rules'] = [
    {
        // We're specifying native_modules in the test because the asset relocator loader generates a
        // "fake" .node file which is really a cjs file.
        test: /native_modules[/\\].+\.node$/,
        use: 'node-loader',
    },
    {
        test: /[/\\]node_modules[/\\].+\.(m?js|node)$/,
        parser: { amd: false },
        use: {
            loader: '@vercel/webpack-asset-relocator-loader',
            options: {
                outputAssetBase: 'native_modules',
            },
        },
    },
    {
        test: /\.css$/,
        use: [
            {
                loader: 'style-loader',
                options: {},
            },
            {
                loader: 'css-loader',
            },
        ],
    },
    {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
    },
    {
        test: /\.(jpe?g|png|gif|svg|webp)$/,
        loader: 'file-loader',
        options: {
            name: '[path][name].[ext]',
        }
    },
    {
        test: /\.tsx?/,
        use: {
            loader: 'ts-loader',
            options: {
                transpileOnly: true,
                experimentalWatchApi: true,
            },
        },
    },
]
