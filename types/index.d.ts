interface ExtFileEntry {
  extPath: string;
  name: string;
  isFile: boolean;
  modificationDate: number;
}

interface Window {
  FileExternal: {
    dirChooser:() => Promise<string>;
    listDir:(extRootUri: string, extPath: string) => Promise<Array<ExtFileEntry>>;
    readFile:(extRootUri: string, extFilePath: string) => Promise<string>;
    readFileBinary:(extRootUri: string, extFilePath: string) => Promise<Array<number>>;
    remove: (extRootUri: string, extPath: string) => Promise<void>;
    createDir: (extRootUri: string, extPath: string, dir: string) => Promise<void>;
    writeFile: (extRootUri: string, extPath: string, file: string, data: string) => Promise<void>;
    copyAssetsToExternal: (sourcePath: string, extRootUri: string, extPath: string) => Promise<void>;
  }
}