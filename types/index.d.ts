interface Entry {
  path: string;
  name: string;
  isFile: boolean;
  modificationDate: number;
}

interface Window {
  FileExternal: {
    dirChooser:() => Promise<string>;
    listDir:(path: string, dir: string) => Promise<Array<Entry>>;
  }
}