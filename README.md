# Birds

An example demonstrating a way to structure a clojure and clojurescript application using facebook's reactjs.

## What the app does

This app is a tool for birdwatchers and teams of birdwatchers to keep track of the birds they've seen. We're currently seeking venture capital.

## To run
* compile the shared code with `lein cljx once`
* compile the clojurescript with `lein cljsbuild once`
* start a repl with `lein repl` 
* evaluate `(reset)`
* Go to localhost:8000 in your browser

## Read more

This blog post talks about the concepts implemented in this code base:
[How I Structured a Clojure(+Script) Web App](http://tomconnors.github.io/blog/2013/12/03/how-i-structured-a-clojure-plus-script-web-app/)

## Known Problems

This is either a problem with lein, cljx, or my usage of one of those two things. When I clean out all generated code, then run `lein cljx auto`, the task fails because my .clj code can't compile. Because it depends on .cljx code. That hasn't been compiled to .clj code yet. ([like this](http://en.wikipedia.org/wiki/Ouroboros).) A simple fix to this nuisance is to comment out all `source-paths` properties in the project.clj, then run `lein cljx once`, then uncomment those properties, then resume a normal workflow. Because that's such a pain in the ass, I've committed the generated code under target/generated to this repo - hopefully the first time you attempt to run a repl or otherwise compile this project you won't have to deal with this.

I don't use the functionality provided by friend in cases where I probably should; In birds.web-page I include the `friend/authenticate` middleware, but don't use the friend-login route at all. This business could be cleaned up.

Errors with austin: Austin (a clojurescript repl tool) allows starting a repl, but it throws an error about the pump macros.

CSS styling is god awful. PRs to make the example app less hideous would be accepted, but I don't really care about the looks, given that this isn't an example of how to make handsome websites.

## Unknown Problems

The source of this project is drawn primarily from another (closed-source) project. If there's cruft, and there is, that's why. PRs to eliminate unused code are welcome. 
There's also surely stylistic, security, and functionality concerns. Again, PRs welcome.
If there's a common task when developing clojurescript web apps without an implementation in this codebase, please open an issue or submit a PR.