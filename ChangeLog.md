# Changelog

## Version 1.1.1
* Consider gateway timeouts as a bad gateway exception.

## Version 1.1.0
* JournalManager: add a function to fetch a journal by UID

## Version 1.0.4
* Make all of the API classes serializable (makes it easier to use them).

## Version 1.0.3
* Expose the lastUid of journals so we know if we need to fetch a journal or not.
* Add a TokenAuthenticator utility class for using token authorization

## Version 1.0.2
* Add missing model files (CollectionInfo and SyncEntry)

## Version 1.0.1
* Initial version, imported from the android client repo and released to maven central
