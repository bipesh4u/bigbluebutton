import Logger from '/imports/startup/server/logger';
import Cursor from '/imports/api/cursor';

export default function updateCursor(meetingId, x = 0, y = 0) {
  check(meetingId, String);
  check(x, Number);
  check(y, Number);

  const selector = {
    meetingId,
  };

  const modifier = {
    $set: {
      meetingId,
      x,
      y,
    },
  };

  const cb = (err, numChanged) => {
    if (err) {
      return Logger.error(`Upserting cursor to collection: ${err}`);
    }

    const { insertedId } = numChanged;
    if (insertedId) {
      return Logger.debug(`Initialized cursor meeting=${meetingId}`);
    }

    if (numChanged) {
      return Logger.verbose(`Updated cursor meeting=${meetingId}`);
    }
  };

  return Cursor.upsert(selector, modifier, cb);
};
