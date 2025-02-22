/***************************************************************************
    copyright            : (C) 2011 by Lukas Lalinsky
    email                : lalinsky@gmail.com
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

#include "tbytevectorstream.h"

#include <cstring>

#include "tstring.h"

using namespace TagLib;

class ByteVectorStream::ByteVectorStreamPrivate
{
public:
  ByteVectorStreamPrivate(const ByteVector &data) :
    data(data)
  {
  }

  ByteVector data;
  offset_t position { 0 };
};

////////////////////////////////////////////////////////////////////////////////
// public members
////////////////////////////////////////////////////////////////////////////////

ByteVectorStream::ByteVectorStream(const ByteVector &data) :
  d(std::make_unique<ByteVectorStreamPrivate>(data))
{
}

ByteVectorStream::~ByteVectorStream() = default;

FileName ByteVectorStream::name() const
{
  return ""; // XXX do we need a name?
}

ByteVector ByteVectorStream::readBlock(size_t length)
{
  if(length == 0)
    return ByteVector();

  ByteVector v = d->data.mid(static_cast<unsigned int>(d->position),
                             static_cast<unsigned int>(length));
  d->position += v.size();
  return v;
}

void ByteVectorStream::writeBlock(const ByteVector &data)
{
  unsigned int size = data.size();
  if(d->position + size > length()) {
    truncate(d->position + size);
  }
  memcpy(d->data.data() + d->position, data.data(), size);
  d->position += size;
}

void ByteVectorStream::insert(const ByteVector &data, offset_t start, size_t replace)
{
  offset_t sizeDiff = data.size() - replace;
  if(sizeDiff < 0) {
    removeBlock(start + data.size(), -sizeDiff);
  }
  else if(sizeDiff > 0) {
    truncate(length() + sizeDiff);
    offset_t readPosition  = start + replace;
    offset_t writePosition = start + data.size();
    memmove(d->data.data() + writePosition, d->data.data() + readPosition, length() - sizeDiff - readPosition);
  }
  seek(start);
  writeBlock(data);
}

void ByteVectorStream::removeBlock(offset_t start, size_t length)
{
  offset_t readPosition = start + length;
  offset_t writePosition = start;
  if(readPosition < ByteVectorStream::length()) {
    offset_t bytesToMove = ByteVectorStream::length() - readPosition;
    memmove(d->data.data() + writePosition, d->data.data() + readPosition, bytesToMove);
    writePosition += bytesToMove;
  }
  d->position = writePosition;
  truncate(writePosition);
}

bool ByteVectorStream::readOnly() const
{
  return false;
}

bool ByteVectorStream::isOpen() const
{
  return true;
}

void ByteVectorStream::seek(offset_t offset, Position p)
{
  switch(p) {
  case Beginning:
    d->position = offset;
    break;
  case Current:
    d->position += offset;
    break;
  case End:
    d->position = length() + offset; // offset is expected to be negative
    break;
  }
}

void ByteVectorStream::clear()
{
}

offset_t ByteVectorStream::tell() const
{
  return d->position;
}

offset_t ByteVectorStream::length()
{
  return d->data.size();
}

void ByteVectorStream::truncate(offset_t length)
{
  d->data.resize(static_cast<unsigned int>(length));
}

ByteVector *ByteVectorStream::data()
{
  return &d->data;
}
