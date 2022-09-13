# minimalist-fulcro-template

A template for starting a new, minimalistic, full-stack Fulcro application. Intended for playing with and learning Fulcro, not for production apps, and therefore simpler than the official [fulcro-template](https://github.com/fulcrologic/fulcro-template). It is a good starting point for your learning projects that is hopefully simple enough for you to understand.

TIP: For an even simpler template with an in-browser backend, see [minimalist-fulcro-template-backendless](https://github.com/holyjak/minimalist-fulcro-template-backendless).

## Creating a new application from the template

[Download](https://github.com/holyjak/minimalist-fulcro-template/archive/refs/heads/main.zip) or clone this repository to your computer and start hacking away.

## Explanation

You will run shadow-cljs, which will watch, compile, and update the sources and separately you will start a HTTP server from the REPL to serve the application and process Pathom requests.

## Usage

Prerequisites: [same as shadow-cljs'](https://github.com/thheller/shadow-cljs#requirements).

First, install frontend dependencies via npm, yarn, or similar:

    npm install # or yarn install # reportedly yarn < v3

then start the application either via

    npx shadow-cljs watch main

or, if you have [Babashka](https://babashka.org/) installed, via

    bb run

NOTE: For Calva, it is best to start a client build and REPL from the editor - [run Jack-in](https://calva.io/connect/#jack-in-let-calva-start-the-repl-for-you), selecting _shadow-cljs_ then the `:main` build. 

Now **start the server**: load `com.example.server.server` into the Shadow REPL - this will also evaluate the `(defonce stop-fn (atom start))` line, starting the server.

NOTE: Now if you ever change Pathom resolvers or something, run the restart code in the `(comment ...)` in the server ns.

Finally, navigate to http://localhost:8008. Note: You can switch to the browser REPL by evaluating `(shadow/repl :main)` in the REPL. (Calva does the latter for you).

### Create a standalone build

You can also compile the sources into a directory via

    npx shadow-cljs release main
    # or: bb build

## Why is this not suitable for production?

No thought was given to security, performance, monitoring, error tracking and other important production concerns. It also bakes in fulcro-troubleshooting, which you do not want unnecessarily increasing your bundle size in production settings. So if you want to use the template as a starting point for a production application, you will need to add those yourself.

## License

Copyleft © 2022 Jakub Holý

Distributed under the [Unlicense](https://unlicense.org/).
