# craw
Web crawler for NSWI153 course at MFF Charles University.
The objective is to implement a Web crawler with a web-based interface.

## Site management

The application should allow a user to keep track of website records to crawl. For each website record the user can specify:
- URL - where the crawler should start.
- Boundary RegExp - when the crawler found a link, the link must match this expression in order to be followed. User is required to provide value for this.
- Periodicity (minute, hour, day) - how often should the site be crawled.
- Label - user given label.
- Active / Inactive - if inactive, the site is not crawled based on the Periodicity.
- Tags - user given strings.
  The application should implement common CRUD operation. The user can see website records in a paginated view. The view can be filtered using URL, Label, and/or Tags. The view can be sorted based on the URL or the last time a site was crawled. The view must contain Label, Periodicity, Tags, time of last execution, the status of last execution.
  Execution management

Each active website record is executed based on the periodicity. Each execution creates a new execution. For example, if the Periodicity is an hour, the executor tries to crawl the site every hour ~ last execution time + 60 minutes. You may use start of the last execution or end of the last execution. While doing the first may not be safe, id does not matter here. If there is no execution for a given record and the record is active the crawling is started as soon as possible, this should be implemented using some sort of a queue. A user can list all the executions, or filter all executions for a single website record. In both cases, the list must be paginated. The list must contain website record's label, execution status, start/end time, number of sites crawled. A user can manually start an execution for a given website record. When a website records is deleted all executions and relevant data are removed as well.
## Executor

The executor is responsible for executing, i.e. crawling selected websites. Crawler downloads the website and looks for all hyperlinks. For each detected hyperlink that matches the website record Boundary RegExp the crawler also crawls the given page. For each crawled website it creates a record with the following data:
- URL
- Crawl time
- Title - page title
- Links - List of outgoing links
  Crawled data are stored as a part of the website record, so the old data are lost once the new execution is successfully finished. It must be possible to run multiple executions at once.
## Visualisation

For selected website records (active selection) user can view a map of crawled pages as a graph. Nodes are websites/domains. There is an oriented edge (connection) from one node to another if there is a hyperlink connecting them in a given direction. The graph should also contain nodes for websites/domains that were not crawled due to a Boundary RegExp restriction. Those nodes will have different visuals so they can be easily identified. A user can switch between website view and domain view. In the website view, every website is represented by a node. In the domain view, all nodes from a given domain (use a full domain name) are replaced by a single node. By double-clicking, the node the user can open node detail. For crawled nodes, the details contain URL, Crawl time, and list of website record that crawled given node. The user can start new executions for one of the listed website records. For other nodes, the detail contains only URL and the user can create and execute a new website record. The newly created website record is automatically added to the active selection and mode is changed to live. The visualisation can be in live or static mode. In static data are not refreshed. In the live mode data are periodically updated based on the new executions for active selection.
If a single node is crawled by multiple executions from active selection data from lates execution are used for detail.
Use page title or URL, in given order of preference, as a node label. In domain node employ the URL.
API

The website record and execution CRUD must be exposed using HTTP-based API documented using OpenAPI / Swagger. Crawled data of all website records can be queried using GraphQL. The GraphQL model must "implement" the following schema:

```
type Query{
websites: [WebPage!]!
nodes(webPages: [ID!]): [Node!]!
}

type WebPage{
identifier: ID!
label: String!
url: String!
regexp: String!
tags: [String!]!
active: Boolean!
}

type Node{
title: String
url: String!
crawlTime: String
links: [Node!]!
owner: WebPage!
}
```
## Deployment

The whole application can be deployed using docker-compose.

```
git clone ...
docker compose up
```

## Others
- The application must provide a reasonable level of user experience, be reasonably documented with reasonable code style.
- No documentation is required, but you will be asked to showcase and comment on the final software.
- When scraping a site follow only <a href="..." ....
- The scraping parallelism must utilize more then one thread. It is not sufficient, for the purpose of the assignment, to employ just NodeJS and argument of async IO. For example, NodeJS support worker threads - using them for crawling is ok.