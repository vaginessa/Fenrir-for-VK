/***************************************************************************
    copyright            : (C) 2002 - 2008 by Scott Wheeler
    email                : wheeler@kde.org
 ***************************************************************************/

/***************************************************************************
 *   This library is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Lesser General Public License version   *
 *   2.1 as published by the Free Software Foundation.                     *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful, but   *
 *   WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA         *
 *   02110-1301  USA                                                       *
 *                                                                         *
 *   Alternatively, this file is available under the Mozilla Public        *
 *   License Version 1.1.  You may obtain a copy of the License at         *
 *   http://www.mozilla.org/MPL/                                           *
 ***************************************************************************/

#include "id3v2header.h"

#include <bitset>

#include "tstring.h"
#include "tdebug.h"
#include "id3v2footer.h"
#include "id3v2synchdata.h"

using namespace TagLib;
using namespace ID3v2;

class Header::HeaderPrivate
{
public:
  unsigned int majorVersion { 4 };
  unsigned int revisionNumber { 0 };

  bool unsynchronisation { false };
  bool extendedHeader { false };
  bool experimentalIndicator { false };
  bool footerPresent { false };

  unsigned int tagSize { 0 };
};

////////////////////////////////////////////////////////////////////////////////
// static members
////////////////////////////////////////////////////////////////////////////////

unsigned int Header::size()
{
  return 10;
}

ByteVector Header::fileIdentifier()
{
  return ByteVector::fromCString("ID3");
}

////////////////////////////////////////////////////////////////////////////////
// public members
////////////////////////////////////////////////////////////////////////////////

Header::Header() :
  d(std::make_unique<HeaderPrivate>())
{
}

Header::Header(const ByteVector &data) :
  d(std::make_unique<HeaderPrivate>())
{
  parse(data);
}

Header::~Header() = default;

unsigned int Header::majorVersion() const
{
  return d->majorVersion;
}

void Header::setMajorVersion(unsigned int version)
{
  d->majorVersion = version;
}

unsigned int Header::revisionNumber() const
{
  return d->revisionNumber;
}

bool Header::unsynchronisation() const
{
  return d->unsynchronisation;
}

bool Header::extendedHeader() const
{
  return d->extendedHeader;
}

bool Header::experimentalIndicator() const
{
  return d->experimentalIndicator;
}

bool Header::footerPresent() const
{
  return d->footerPresent;
}

unsigned int Header::tagSize() const
{
  return d->tagSize;
}

unsigned int Header::completeTagSize() const
{
  if(d->footerPresent)
    return d->tagSize + size() + Footer::size();
  return d->tagSize + size();
}

void Header::setTagSize(unsigned int s)
{
  d->tagSize = s;
}

void Header::setData(const ByteVector &data)
{
  parse(data);
}

ByteVector Header::render() const
{
  ByteVector v;

  // add the file identifier -- "ID3"
  v.append(fileIdentifier());

  // add the version number -- we always render a 2.4.0 tag regardless of what
  // the tag originally was.

  v.append(static_cast<char>(majorVersion()));
  v.append(static_cast<char>(0));

  // Currently we don't actually support writing extended headers, footers or
  // unsynchronized tags, make sure that the flags are set accordingly.

  d->extendedHeader = false;
  d->footerPresent = false;
  d->unsynchronisation = false;

  // render and add the flags
  std::bitset<8> flags;

  flags[7] = d->unsynchronisation;
  flags[6] = d->extendedHeader;
  flags[5] = d->experimentalIndicator;
  flags[4] = d->footerPresent;

  v.append(static_cast<char>(flags.to_ulong()));

  // add the size
  v.append(SynchData::fromUInt(d->tagSize));

  return v;
}

////////////////////////////////////////////////////////////////////////////////
// protected members
////////////////////////////////////////////////////////////////////////////////

void Header::parse(const ByteVector &data)
{
  if(data.size() < size())
    return;

  // do some sanity checking -- even in ID3v2.3.0 and less the tag size is a
  // synch-safe integer, so all bytes must be less than 128.  If this is not
  // true then this is an invalid tag.

  // note that we're doing things a little out of order here -- the size is
  // later in the bytestream than the version

  const ByteVector sizeData = data.mid(6, 4);

  if(sizeData.size() != 4) {
    d->tagSize = 0;
    debug("TagLib::ID3v2::Header::parse() - The tag size as read was 0 bytes!");
    return;
  }

  for(const auto &size : sizeData) {
    if(static_cast<unsigned char>(size) >= 128) {
      d->tagSize = 0;
      debug("TagLib::ID3v2::Header::parse() - One of the size bytes in the id3v2 header was greater than the allowed 128.");
      return;
    }
  }

  // The first three bytes, data[0..2], are the File Identifier, "ID3". (structure 3.1 "file identifier")

  // Read the version number from the fourth and fifth bytes.
  d->majorVersion = data[3];   // (structure 3.1 "major version")
  d->revisionNumber = data[4]; // (structure 3.1 "revision number")

  // Read the flags, the first four bits of the sixth byte.
  std::bitset<8> flags(data[5]);

  d->unsynchronisation     = flags[7]; // (structure 3.1.a)
  d->extendedHeader        = flags[6]; // (structure 3.1.b)
  d->experimentalIndicator = flags[5]; // (structure 3.1.c)
  d->footerPresent         = flags[4]; // (structure 3.1.d)

  // Get the size from the remaining four bytes (read above)

  d->tagSize = SynchData::toUInt(sizeData); // (structure 3.1 "size")
}
