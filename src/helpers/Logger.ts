const DEBUG = false;

export function Log(debug: boolean, ...message: any[]) {
  const args = ['[Batch]'].concat(Array.prototype.slice.call(arguments, 1));
  if (DEBUG && debug) {
    console.debug.apply(console, args);
  } else if (debug === false) {
    console.log.apply(console, args);
  }
}

export default Log;
