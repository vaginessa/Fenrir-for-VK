/***************************************************************************
    copyright            : (C) 2002 - 2008 by Scott Wheeler
    email                : wheeler@kde.org

    copyright            : (C) 2006 by Urs Fleisch
    email                : ufleisch@users.sourceforge.net
 ***************************************************************************/

/***************************************************************************
 *   This library is free software; you can redistribute it and/or modify  *
 *   it  under the terms of the GNU Lesser General Public License version  *
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

#include "unsynchronizedlyricsframe.h"

#include <utility>

#include "tbytevectorlist.h"
#include "tdebug.h"
#include "tpropertymap.h"
#include "id3v2tag.h"

using namespace TagLib;
using namespace ID3v2;

class UnsynchronizedLyricsFrame::UnsynchronizedLyricsFramePrivate
{
public:
  String::Type textEncoding { String::Latin1 };
  ByteVector language;
  String description;
  String text;
};

////////////////////////////////////////////////////////////////////////////////
// public members
////////////////////////////////////////////////////////////////////////////////

UnsynchronizedLyricsFrame::UnsynchronizedLyricsFrame(String::Type encoding) :
  Frame("USLT"),
  d(std::make_unique<UnsynchronizedLyricsFramePrivate>())
{
  d->textEncoding = encoding;
}

UnsynchronizedLyricsFrame::UnsynchronizedLyricsFrame(const ByteVector &data) :
  Frame(data),
  d(std::make_unique<UnsynchronizedLyricsFramePrivate>())
{
  setData(data);
}

UnsynchronizedLyricsFrame::~UnsynchronizedLyricsFrame() = default;

String UnsynchronizedLyricsFrame::toString() const
{
  return d->text;
}

ByteVector UnsynchronizedLyricsFrame::language() const
{
  return d->language;
}

String UnsynchronizedLyricsFrame::description() const
{
  return d->description;
}

String UnsynchronizedLyricsFrame::text() const
{
  return d->text;
}

void UnsynchronizedLyricsFrame::setLanguage(const ByteVector &languageEncoding)
{
  d->language = languageEncoding.mid(0, 3);
}

void UnsynchronizedLyricsFrame::setDescription(const String &s)
{
  d->description = s;
}

void UnsynchronizedLyricsFrame::setText(const String &s)
{
  d->text = s;
}


String::Type UnsynchronizedLyricsFrame::textEncoding() const
{
  return d->textEncoding;
}

void UnsynchronizedLyricsFrame::setTextEncoding(String::Type encoding)
{
  d->textEncoding = encoding;
}

PropertyMap UnsynchronizedLyricsFrame::asProperties() const
{
  PropertyMap map;
  String key = description().upper();
  if(key.isEmpty() || key == "LYRICS")
    map.insert("LYRICS", text());
  else
    map.insert("LYRICS:" + key, text());
  return map;
}

UnsynchronizedLyricsFrame *UnsynchronizedLyricsFrame::findByDescription(const ID3v2::Tag *tag, const String &d) // static
{
  for(const auto &lyrics : std::as_const(tag->frameList("USLT"))) {
    auto frame = dynamic_cast<UnsynchronizedLyricsFrame *>(lyrics);
    if(frame && frame->description() == d)
      return frame;
  }
  return nullptr;
}
////////////////////////////////////////////////////////////////////////////////
// protected members
////////////////////////////////////////////////////////////////////////////////

void UnsynchronizedLyricsFrame::parseFields(const ByteVector &data)
{
  if(data.size() < 5) {
    debug("An unsynchronized lyrics frame must contain at least 5 bytes.");
    return;
  }

  d->textEncoding = static_cast<String::Type>(data[0]);
  d->language = data.mid(1, 3);

  int byteAlign
    = d->textEncoding == String::Latin1 || d->textEncoding == String::UTF8 ? 1 : 2;

  ByteVectorList l =
    ByteVectorList::split(data.mid(4), textDelimiter(d->textEncoding), byteAlign, 2);

  if(l.size() == 2) {
    if(d->textEncoding == String::Latin1) {
      d->description = Tag::latin1StringHandler()->parse(l.front());
      d->text = Tag::latin1StringHandler()->parse(l.back());
    } else {
      d->description = String(l.front(), d->textEncoding);
      d->text = String(l.back(), d->textEncoding);
    }
  }
}

ByteVector UnsynchronizedLyricsFrame::renderFields() const
{
  StringList sl;
  sl.append(d->description);
  sl.append(d->text);

  const String::Type encoding = checkTextEncoding(sl, d->textEncoding);

  ByteVector v;

  v.append(static_cast<char>(encoding));
  v.append(d->language.size() == 3 ? d->language : "XXX");
  v.append(d->description.data(encoding));
  v.append(textDelimiter(encoding));
  v.append(d->text.data(encoding));

  return v;
}

////////////////////////////////////////////////////////////////////////////////
// private members
////////////////////////////////////////////////////////////////////////////////

UnsynchronizedLyricsFrame::UnsynchronizedLyricsFrame(const ByteVector &data, Header *h) :
  Frame(h),
  d(std::make_unique<UnsynchronizedLyricsFramePrivate>())
{
  parseFields(fieldData(data));
}
