const DEBUG = false;

export function Log(debug: boolean, ...message: unknown[]): void {
  if (DEBUG && debug) {
    console.debug(...message);
  } else if (debug === false) {
    console.log(...message);
  }
}

export default Log;
