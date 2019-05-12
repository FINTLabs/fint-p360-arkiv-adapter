# FINT P360 adapter
This adapter connects Tietos Public 360 to FINT.

It uses the `SIF` webservice. At the moment it support the `SOAP` version.

The adapter uses the following services:
- CaseService
- DocumentService
- ContactService
- FileService
- SupportService

# Properties

| Properties                                            | Default                                  | Description   |
| :---------------------------------------------------- | :--------------------------------------- | :------------ |
| fint.p360.user                                        |                                          |               |
| fint.p360.password                                    |                                          |               |
| fint.p360.endpoint-base-url                           |                                          |               |
| fint.file-repository.cache-directory                  | file-cache                               |               |
| fint.file-repository.cache-spec                       | expireAfterAccess=5m,expireAfterWrite=7m |               |
| fint.kulturminne.tilskudd-fartoy.arkivdel             |                                          |               |
| fint.kulturminne.tilskudd-fartoy.sub-archive          |                                          |               |
| fint.kulturminne.tilskudd-fartoy.keywords             |                                          |               |
| fint.kulturminne.tilskudd-fartoy.achive-code-type     |                                          |               |
| fint.kulturminne.tilskudd-fartoy.intitial-case-status | `B`                                      |               |
| fint.p360.tables.document-category                    | `Document category`                      |               |
| fint.p360.tables.journal-status                       | `Journal status`                         |               |
| fint.p360.tables.document-status                      | `Document status`                        |               |
| fint.p360.tables.contact-role                         | `Activity - Contact role`                |               |
| fint.p360.tables.case-status                          | `Case status`                            |               |


