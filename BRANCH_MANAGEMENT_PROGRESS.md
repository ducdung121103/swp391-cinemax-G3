## Database Setup

### Schema Changes
- Added cinema_branches table
- Columns: branch_id, branch_name, address, phone, email, manager_id, is_active
- Created indexes on branch_name and is_active

### Migration Files
- Created migration script for cinema_branches table
- Updated database context configuration
## Model Implementation

### CinemaBranch Model
- Created CinemaBranch.java with Lombok annotations
- Properties: branchId, branchName, address, phone, email, managerId, isActive, timestamps
- Added validation annotations
- Implemented toString, equals, hashCode methods
