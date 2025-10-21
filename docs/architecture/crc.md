[⬅️Back](../architecture/architecture-main.md)
## Event Card


| Responsibilities | Collaborators |
| --- | --- |
| Maintain event details (name, description, time/place, capacity, poster ref, QR token, organizer) | Organizer, QRManager, ImageService |
| Manage registration period (open/close) | Organizer, WaitingListManager |
| Track capacity (remaining spots / full) | WaitingListManager |
| Enforce optional waitlist size limit | WaitingListManager |


---


## Entrant
| Responsibilities | Collaborators |
| --- | --- |
| Maintain personal information (profile data) | ProfileManager |
| Join/unjoin waiting lists | WaitingListManager, Event |
| Receive and respond to notifications (accept/decline invitations) | NotificationManager, Invitation |
| View event history (past registrations and outcomes) | ProfileManager |

---


## Organizer
| Responsibilities | Collaborators |
| --- | --- |
| Create and manage events | Event, QRManager |
| Set registration period and capacity | Event |
| Upload and update event posters | ImageService |
| Manage entrant lists (waiting, invited, cancelled) | WaitingListManager, Invitation |
| Trigger lottery draws and replacements (not perform directly) | LotterySystem |
| Send notifications to entrants | NotificationManager |
| Export final list of participants  | CSVExporter |

---
## Administrator Card


| Responsibilities | Collaborators |
| --- | --- |
| Manage and moderate events, users, and organizers | Event, ProfileManager |
| Remove inappropriate content (events, posters, profiles) | ImageService, ProfileManager |
| Review logs of notifications and activities | AuditLogger |
| Browse events, profiles, and images | Event, ProfileManager, ImageService |
| Revoke organizer privileges | ProfileManager |


---

## WaitingListManager
| Responsibilities | Collaborators |
| --- | --- |
| Add or remove entrants from waiting lists | Event, Entrant |
| Track waiting list counts per event | Event |
| Enforce event rules (registration open, waitlist limit) | Event |
| Provide entrants to the lottery system for selection | LotterySystem |

---
## ProfileManager
| Responsibilities | Collaborators |
| --- | --- |
| Handle onboarding and profile updates | Entrant, Organizer, Administrator |
| Store entrant, organizer, and admin personal info | Entrant, Organizer, Administrator |
| Delete profiles and related data on request | Entrant, Administrator |
| Manage device-based identity (create/verify device ID) | DeviceIdAuth |
| Support admin-initiated account removals | Administrator |

---

## NotificationManager
| Responsibilities | Collaborators |
| --- | --- |
| Send notifications (win/lose, broadcasts) | Entrant, Organizer |
| Handle opt-in/opt-out preferences | Entrant |
| Notify groups (waiting, invited, cancelled) | Organizer, LotterySystem |
| Log notification history | AuditLogger |

---
## QRManager
| Responsibilities | Collaborators |
| --- | --- |
| Generate QR code and token for event | Event, Organizer |
| Resolve scanned QR to event details (deep link) | Event |

---
## LotterySystem
| Responsibilities | Collaborators |
| --- | --- |
| Randomly select entrants from waiting list | WaitingListManager |
| Create invitations for selected entrants | Invitation |
| Run replacement draws when declined/cancelled | Invitation |
| Provide lottery policy/criteria text | Event |
| Record draw results in logs | AuditLogger |

---

## GeoLocationService
| Responsibilities | Collaborators |
| --- | --- |
| Capture entrant location at join time | Entrant, Event |
| Show map of join locations to organizers | Organizer, Event |
| Enforce geo requirement when enabled | Event, WaitingListManager |

---

## AuditLogger
| Responsibilities | Collaborators |
| --- | --- |
| Record system actions (notifications, draws, deletions) | NotificationManager, LotterySystem, Administrator |
| Provide admin review of logs | Administrator |

---

## Invitation
| Responsibilities | Collaborators |
| --- | --- |
| Represent an invitation to join an event | Entrant, Event |
| Accept or decline invitation | Entrant |
| Track status (PENDING, ACCEPTED, DECLINED, EXPIRED, CANCELLED) | LotterySystem |
| Trigger enrollment on accept | Enrollment |

---

## Enrollment
| Responsibilities | Collaborators |
| --- | --- |
| Keep final list of accepted participants | Event, Invitation |
| Allow organizer to cancel enrollment | Organizer |
| Provide data for CSV export | CSVExporter |

---

## CSVExporter
| Responsibilities | Collaborators |
| --- | --- |
| Export final enrolled list to CSV | Organizer, Enrollment |

---

## DeviceIdAuth
| Responsibilities | Collaborators |
| --- | --- |
| Identify entrant by device (no username/password) | Entrant, ProfileManager |
| Prevent duplicate joins by same device for event | WaitingListManager |

---

## ImageService
| Responsibilities | Collaborators |
| --- | --- |
| Upload and update event poster image | Organizer, Event |
| Remove images when required | Administrator |
| Provide poster URL/reference to Event | Event |

_Last updated: [2025-10-21]
