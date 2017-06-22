interface Entry {
  path: string;
  name: string;
  isFile: boolean;
  modificationDate: number;
}

interface Window {
  FileExternal: {
    dirChooser:() => Promise<string>;
    listDir:(rootUri: string, dir: string) => Promise<Array<Entry>>;
    readFile:(rootUri: string, file: string) => Promise<string>;
  }
}