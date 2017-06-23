interface Entry {
  path: string;
  name: string;
  isFile: boolean;
  modificationDate: number;
}

interface Window {
  FileExternal: {
    dirChooser:() => Promise<string>;
    listDir:(extRootUri: string, dir: string) => Promise<Array<Entry>>;
    readFile:(extRootUri: string, file: string) => Promise<string>;
    remove: (extRootUri: string, extPath: string) => Promise<void>;
    createDir: (extRootUri: string, extPath: string, dir: string) => Promise<void>;
    copyAssetsToExternal: (sourcePath: string, extRootUri: string, extPath: string) => Promise<void>;
  }
}