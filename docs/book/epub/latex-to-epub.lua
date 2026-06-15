local callout_classes = {
  principlebox = "principle",
  pitfallbox = "pitfall",
  detailbox = "detail",
  coruscobox = "corusco",
  migrationbox = "migration",
  examplebox = "example",
  exercisebox = "exercise"
}

local callout_prefixes = {
  pitfall = "Pitfall: ",
  detail = "Less-known detail: ",
  corusco = "What Corusco changes: ",
  migration = "From plain Swing to Corusco: ",
  example = "Companion example: "
}

local function stringify(blocks)
  return pandoc.utils.stringify(blocks):gsub("^%s+", ""):gsub("%s+$", "")
end

local function first_plain_text(blocks)
  if #blocks == 0 then
    return nil
  end
  local first = blocks[1]
  if first.t ~= "Para" and first.t ~= "Plain" then
    return nil
  end
  if #first.content == 0 then
    return nil
  end
  local first_inline = first.content[1]
  if first_inline.t ~= "Span" and first_inline.t ~= "Strong" and first_inline.t ~= "Emph" then
    return nil
  end
  local title = pandoc.utils.stringify(first_inline):gsub("^%s+", ""):gsub("%s+$", "")
  if title == "" then
    return nil
  end
  table.remove(first.content, 1)
  if #first.content > 0 and first.content[1].t == "Space" then
    table.remove(first.content, 1)
  end
  return title
end

local function callout_title(kind, title)
  local prefix = callout_prefixes[kind] or ""
  if title == nil or title == "" then
    if kind == "exercise" then
      title = "Exercises"
    else
      title = kind:gsub("^%l", string.upper)
    end
  end
  return prefix .. title
end

local function normalize_callout(div, kind)
  local title = first_plain_text(div.content)
  local title_block = pandoc.Plain({
    pandoc.Span(
      { pandoc.Str(callout_title(kind, title)) },
      pandoc.Attr("", { "callout-title" }, {})
    )
  })

  table.insert(div.content, 1, title_block)
  div.classes = { "callout", "callout-" .. kind }
  return div
end

function Div(div)
  for latex_class, kind in pairs(callout_classes) do
    if div.classes:includes(latex_class) then
      return normalize_callout(div, kind)
    end
  end

  if div.classes:includes("titlepage") then
    div.classes = { "title-page" }
    return div
  end

  return div
end

function Span(span)
  local text = stringify(span.content)
  if text == "A4 book edition" then
    return pandoc.Span({ pandoc.Str("EPUB edition") }, span.attr)
  end

  if text == "Swing with Corusco" then
    span.classes:insert("book-title")
    return span
  end

  return span
end

function Image(img)
  img.attributes.style = nil
  img.attributes.width = nil
  img.attributes.height = nil
  return img
end

function CodeBlock(block)
  block.attributes["data-caption"] = nil
  return block
end

function Figure(fig)
  fig.attributes["latex-placement"] = nil
  fig.attributes["data-latex-placement"] = nil
  return fig
end

function Table(tbl)
  if tbl.colspecs then
    for i, spec in ipairs(tbl.colspecs) do
      tbl.colspecs[i] = { spec[1], pandoc.ColWidthDefault }
    end
  end
  return tbl
end
