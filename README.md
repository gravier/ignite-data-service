# Ignite data service workshop - Building Brazilian property service

This is sample repo for building scala data service with 
[Akka HTTP](http://doc.akka.io/docs/akka-http/current/scala/http/),   
[Quill](https://getquill.io),
[Apache Ignite](https://ignite.apache.org/) libraries  

Goal of the workshop
* intro to ignite
* intro to quill
* setting basic akka-hpp up service
* loading data into ignite
* mapping model with quill to sql
* fetching data from ignite and returning as json
* running tests
* optimizing ignite queries

The service loads data from public dataset of Brazilian Property transactions and loads to a Ignite cache that provides in SQL memory access of loaded data.
Public data set is available [here](https://console.cloud.google.com/marketplace/details/properati/property-data-br)

## Author & license

If you have any questions regarding this project contact:

Evaldas Miliauskas <e.miliauskas@stacktome.com> from [StackTome](https://stacktome.com).

For licensing info see LICENSE file in project's root directory.
