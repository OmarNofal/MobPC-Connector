import open from "open";




interface BrowserService {
    
    /**
     * Opens the given URL using the default browser
     * 
     * @param url The url to open 
     * @param incognito If set to true, this will open the URL in incognito 
     * in MS Edge (this will be changed)
     * @returns true if the link was opened successfuly
     */
    openURLInBrowser(url: string, incognito: boolean): boolean
}

function isValidURL(url: string | URL) {
    try {
        url = new URL(url)
    } catch (e) {
        return false;
    }
    return url.protocol == 'http:' || url.protocol == 'https:'
}


function openURLInBrowser(url: string, incognito: boolean = false): boolean {
    if (isValidURL(url)) {
        if (incognito)
            open(url, {app: {name: 'msedge', arguments: ['-inPrivate']}});
        else
            open(url);
        return true
    } else {
        return false
    }
}

export const browserService: BrowserService = {
    openURLInBrowser: openURLInBrowser
}