import WebpackPlugin from '@electron-forge/plugin-webpack'
import { mainWebpackConfig } from './webpack.main.config'
import { rendererWebpackConfig } from './webpack.renderer.config'
import AutoUnpackNativesPlugin from '@electron-forge/plugin-auto-unpack-natives'

const { FusesPlugin } = require('@electron-forge/plugin-fuses')
const { FuseV1Options, FuseVersion } = require('@electron/fuses')
const path = require('path')


module.exports = {
    packagerConfig: {
        name: 'MobPC Connector',
        asar: true,
        icon: './logo/logo_large'
    },
    rebuildConfig: {},
    makers: [
        {
            name: '@electron-forge/maker-squirrel',
            config: {
                setupIcon: './logo/logo_large.ico'
            },
        },
    ],
    plugins: [
        new AutoUnpackNativesPlugin({}),
        new WebpackPlugin({
            mainConfig: mainWebpackConfig,
            renderer: {
                config: rendererWebpackConfig,
                entryPoints: [
                    {
                        name: 'main_window',
                        html: './src/ui/static/index.html',
                        js: './src/ui/index.tsx',
                        preload: {
                            js: './src/ui/preload.ts',
                        },
                    },
                ],
            },
        }),
        // Fuses are used to enable/disable various Electron functionality
        // at package time, before code signing the application
        new FusesPlugin({
            version: FuseVersion.V1,
            [FuseV1Options.RunAsNode]: false,
            [FuseV1Options.EnableCookieEncryption]: true,
            [FuseV1Options.EnableNodeOptionsEnvironmentVariable]: false,
            [FuseV1Options.EnableNodeCliInspectArguments]: false,
            [FuseV1Options.EnableEmbeddedAsarIntegrityValidation]: true,
            [FuseV1Options.OnlyLoadAppFromAsar]: true,
        }),
    ],
}
