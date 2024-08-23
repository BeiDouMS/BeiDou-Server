# Migrations
The files contained in this directory are intended to be run manually when transitioning from an earlier version of Cosmic to a more recent one. 

Not every version comes with an associated migration script. Only those with breaking changes such as removal of custom assets that would otherwise crash the client.

This is a temporary solution until automatic database migrations are in place.

## How to
Each script is only intended to be run __once__.

When a new migration is available, simply run the SQL script in HeidiSQL (or other SQL client of choice).

If there are multiple new migrations that you haven't run, run them in order starting with the lowest version and ending with the highest version.
