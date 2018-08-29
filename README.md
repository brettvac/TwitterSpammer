# Twitter Spammer
## A bot running on GAE that spams Twits

This is a bot that messages followers of a follower a message of your choice. Choose a Twitter user that has lots of followers to spam, or just use the default.

This bot runs under Google App Engine. It can be used to promote a website, send a spam message, etc.

This bot can send between 1 and 6 tweets at a time. This bot will run only once a day as per the cron.xml file, but you can change this to whatever value you would like.

Be aware that this bot violates Twitter's automation rules: https://help.twitter.com/en/rules-and-policies/twitter-automation