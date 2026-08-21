# Dependency Tracking

The idea is that we are able to cache interface contents by using kotlin delegates to track the use of Player and the context and other parts of interface info, like the slot, in the visual lambdas
like condition and display item creator to reference whether an interface item can be re-used or not.

We use the concept of datasources as something which the interface also uses, and is considered. These are manually defined, and are an enumerable with an identifier.
For a a list of contracts like context, player we assume that the datasource id is stable, so as long as the datasource didnt change, and these dependable fields didnt,
we assume that the result wont change either.
