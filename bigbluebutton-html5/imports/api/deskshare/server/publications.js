import Deskshare from '/imports/api/deskshare';
import { logger } from '/imports/startup/server/logger';

Meteor.publish('deskshare', function (credentials) {
  const { meetingId } = credentials;
  logger.debug(`publishing deskshare for ${meetingId}`);
  return Deskshare.find({ meetingId: meetingId });
});
