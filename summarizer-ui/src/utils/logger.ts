const makeLog = (name: string) => {
  return {
    log: (message: string) => console.log(`[${name.toUpperCase()}]`, message),
    debug: (message: string) =>
      console.debug(`[${name.toUpperCase()}]`, message),
    error: (message: string) =>
      console.error(`[${name.toUpperCase()}]`, message),
  };
};

export default makeLog;
