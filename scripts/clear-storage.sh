#!/usr/bin/env bash

find ./public/storage/photos/thumbnails -name "*.jpg" -type f -delete
find ./public/storage/photos/ -maxdepth 1 -name "*.jpg" -type f -delete 