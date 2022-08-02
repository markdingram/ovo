Ovo Energy (UK) Usage Download & Analysis
=========================================

In the UK certain home electricity tariffs have a cheaper overnight rate - Economy 7 <https://www.ovoenergy.com/guides/energy-guides/economy-7>

As mentioned in that link this is great for electric car owners as charging can be set to run overnight on the cheaper rate.

For much of the last year (alas, until the fixed period expired) the rates were:

- DAY 17.44p per kWh (inc VAT) 
- NIGHT 11.27p per kWh (inc VAT) 

One of the terms to get this tariff was to install a new Smart Meter, which gave the impression of working fine, but statements showed all usage was incorrectly billed at the higher rate! This turned out to be an issue with the Smart Meter - after an (eventual) replacement the billing become accurate once more.

This repo contains some rough'n'ready Clojure files intended for REPL use to download & analyse the usage data to assess the extra charges & agree a refund.

Ovo don't yet support a public API - but <https://github.com/ThePaulAdams/OvOEnergy> suggested it would be straightforward.

Usage
=====

- set environment variables OVO_ACCOUNT_ID, OVO_USERNAME, OVO_PASSWORD  
- use a Clojure REPL (I used VSCode with <https://calva.io/>)
- VS Code command "start project REPL and jack in" - choose deps.edn
- open fetch.clj & VS Code command "Load/Evaluate Current file.."
- start evaluating forms to retrieve a series of JSON files into the data dir
- open analyse.clj & VS Code command "Load/Evaluate Current file.."
- start evaluating forms to analyse the files


Retrospective
=============

I've not written Clojure for a few years, but after coming up to speed with tooling once more (it was the first time I'd used Calva) it didn't take long to develop/henpack away in the REPL to get the answers needed.
