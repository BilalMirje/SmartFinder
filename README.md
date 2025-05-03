# AI Powered SmartFinder - Android App

This is a complete Android application for reporting and finding lost items using Java and a MySQL database with XAMPP.

## Features

- User registration and login
- Report lost items (with photo, location, description)
- Report found items
- View lost/found items
- View item details
- Contact item reporter (feature placeholder)

## Technical Details

- Built with Java for Android
- Uses XAMPP with MySQL database for backend storage
- Direct database operations through PHP scripts
- Material Design UI implementation
- Picasso for image loading
- Uses SharedPreferences for session management

## Project Structure

### Models
- `User.java` - User model with login credentials and token
- `Item.java` - Item model for lost and found items

### Database Helpers
- `DatabaseHelper.java` - Handles all database connections via PHP scripts

### Activities
- `SplashActivity.java` - Displays splash screen and directs to appropriate activity
- `LoginActivity.java` - Handles user login
- `RegisterActivity.java` - Handles user registration
- `MainActivity.java` - Main menu with buttons to access features
- `ReportLostItemActivity.java` - Form to report a lost item
- `ReportFoundItemActivity.java` - Form to report a found item
- `ViewItemsActivity.java` - Tabbed view of lost and found items
- `ItemDetailActivity.java` - Shows detailed information about an item

### Fragments
- `ItemsFragment.java` - Display list of items (lost or found) in a RecyclerView

### Adapters
- `ItemsAdapter.java` - RecyclerView adapter for displaying items
- `ItemsPagerAdapter.java` - ViewPager2 adapter for tabs in ViewItemsActivity

### Utils
- `SessionManager.java` - Manages user session using SharedPreferences
- `ImageUtils.java` - Utility methods for handling images

## Server Setup

The backend should be hosted on XAMPP server:

1. Install XAMPP with PHP and MySQL
2. Create a `smartfinder` database in phpMyAdmin
3. Import the provided SQL schema file
4. Place the PHP scripts in the htdocs folder

### Required PHP Scripts:
- `login.php` - Authenticate user
- `register.php` - Register a new user
- `add_item.php` - Add a new lost or found item
- `get_items.php` - Get list of items by type
- `get_item_details.php` - Get detailed information about an item

## Installation

1. Clone the repository
2. Import into Android Studio
3. Set up XAMPP server with PHP and MySQL
4. Create database and import schema
5. Place PHP scripts in htdocs folder
6. Configure `DatabaseHelper.java` to point to your server

## Configure Server URL

Edit `DatabaseHelper.java` to set the correct URL for your XAMPP server:

```java
private static final String SERVER_URL = "http://your-server-address/smartfinder/";
```

For emulator testing with XAMPP on localhost, use:

```java
private static final String SERVER_URL = "http://10.0.2.2/smartfinder/";
```

## Database Schema

The MySQL database includes these main tables:
- `users` - Store user information
- `items` - Store lost and found items

## License

This project is free to use for educational purposes.

## Credits

Created as a complete project implementation for an Android lost and found app using XAMPP MySQL for data storage. 