## Database Setup

### Schema Changes
- Added cinema_branches table
- Columns: branch_id, branch_name, address, phone, email, manager_id, is_active
- Created indexes on branch_name and is_active

### Migration Files
- Created migration script for cinema_branches table
- Updated database context configuration
