# Meglofriend's Stop Gap

### Work in Progress
Current stage : UI

## Overview

Small javaFx application for temporarily copying directories, while maintaining persistency, to a host location while the session is active. The application is used locally to copy files to a local nodejs server, only when necessary.

### Functions

Save and load presets for quick set up.
Watches original folders for changes and updates the host folder to the changes.
Deletes copied files when session is closed.